/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paperspacecraft.scripting.pattern;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Extends {@link GenericPattern} to provide logic for finding matches within the provided sequence of arbitrary-typed
 * entities honoring pattern elements' quantifiers and groups
 * @param <T>
 */
abstract class QuantifiedMatching<T> extends GenericPattern<T> {

    private int min = 1;
    private int max = 1;

    /* --------
       Accessor
       -------- */

    /**
     * Assigns the expected minimal and maximal numbers of matches
     * @param min Minimal number of matches to find
     * @param max Maximal number of matches to find
     */
    void setQuantifier(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /* --------------
       Matching logic
       -------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    Match findQuantified(List<T> items, int position) {
        return new Finder(items, position, getGroups()).findMatch();
    }

    /**
     * Retrieve the first (atomic) matching result for the given entity sequence
     * @param items    The sequence to which the pattern is applied; an arbitrary-typed list
     * @param position The position from which to start probing for the pattern
     * @return {@link Match} object
     */
    abstract Match findOne(List<T> items, int position);

    /**
     * Initializes a {@link GroupCollection} which is in either enabled or disabled state
     * @return {@code GroupCollection} object
     */
    abstract GroupCollection getGroups();

    /* --------------
       Helper classes
       -------------- */

    /**
     * Encapsulates the logic for finding the sequence of atomic matches per separate pattern elements within the
     * current sequence
     */
    private class Finder {
        private final List<T> items;
        private final int position;
        private final GroupCollection groups;

        private int matchCount;
        private int nextPosition;

        /**
         * Instance constructor
         * @param items           The sequence of arbitrary-types entities to find a match in
         * @param position        The position in the sequence from which to start the search
         * @param groups {@code GroupCollection} accumulating object
         */
        public Finder(List<T> items, int position, GroupCollection groups) {
            this.items = items;
            this.position = position;
            this.groups = groups;
        }

        /**
         * Finds the sequence of atomic matches per separate pattern elements in a loop
         * @return {@link Match} object
         */
        public Match findMatch() {
            matchCount = 0;
            nextPosition = position;

            // Process the first occurrence
            Match currentResult = findOne(items, nextPosition);

            // If the current result is no match but the quantifier allows zero matches, and there's an upstream (i.e.,
            // we are within the terminal member of a capturing group), we must check whether the check can continue
            // even without a match in the current member.
            // Note that we don't create a capturing group for this case
            if (!currentResult.isSuccess() && min == 0 && getUpstreamMatch(items, nextPosition).isSuccess()) {
                return Match.success(nextPosition);
            }

            // If the current result is no match but the quantifier allows zero matches, and there's a sibling pattern
            // element that matches the current item, we report success based on the sibling math
            if (!currentResult.isSuccess() && min == 0) {
                Match siblingResult = getSiblingMatch(items, nextPosition, Match.fail());
                if (siblingResult.isSuccess()) {
                    return siblingResult;
                }
            }

            // In other cases of no match, return an error if the quantifier requires at least one match. However,
            // if it allows zero matches, return not an error but the zero-length success so that the overall routine is
            // not failed.
            // Note that we don't create a capturing group for this case
            if (!currentResult.isSuccess()) {
                if (min == 0) {
                    return getSiblingMatch(items, nextPosition, Match.success(nextPosition));
                }
                return Match.fail();
            }

            // Now there's a match. Process all the available occurrences. We do this in a loop. Counters are
            // incremented at the loop start to account for the match that has already been received
            while (currentResult.isSuccess()) {
                Pair<Match, Boolean> resultAndBreak = consumeMatchAndAdvance(currentResult);
                if (resultAndBreak.getRight()) {
                    return resultAndBreak.getLeft();
                }
                currentResult = resultAndBreak.getLeft();
            }

            // Now the matching search is over. If the exact result is needed, we are here because we haven't reached it.
            // Therefore, fail
            if (isExactNumberNeeded() || matchCount < min) {
                return Match.fail();
            }
            // Otherwise, report local success and pass it on to the next chain link
            return Match
                    .success(position, nextPosition)
                    .and(getSiblingMatch(items, nextPosition, Match.success(nextPosition)))
                    .withGroups(groups.getItems());
        }

        private Pair<Match, Boolean> consumeMatchAndAdvance(Match currentResult) {
            // Add the capturing groups for the current match if needed. (If the current pattern does not support
            // capturing groups, nothing will occur)
            groups.add(matchCount, nextPosition, nextPosition + currentResult.getSize(), currentResult);

            matchCount++;
            nextPosition = currentResult.getEnd();

            if (matchCount == max) {
                Match terminal = Match
                        .success(position, nextPosition)
                        .and(getSiblingMatch(items, nextPosition, Match.success(nextPosition)))
                        .withGroups(groups.getItems());
                return Pair.of(terminal, true);

            } else if (!isExactNumberNeeded() && matchCount >= min) {

                // There are three reasons why we can break away from the loop early...
                Match newCurrentMatch = findOne(items, nextPosition);
                Match siblingMatch = getSiblingMatch(items, nextPosition, Match.fail());

                // 1) The current pattern element is not matched but the sibling is matched
                if (!newCurrentMatch.isSuccess() && siblingMatch.isSuccess()) {
                    Match terminal = Match
                            .success(position, nextPosition)
                            .and(siblingMatch)
                            .withGroups(groups.getItems());
                    return Pair.of(terminal, true);
                }

                // 2) The current pattern element is matched and there's upstream that will not be matched if
                //    the cursor advances but it will be matched if the cursor remains
                //    Else, the current pattern element is NOT matched but there's an upstream match
                Match upstreamMatch = getUpstreamMatch(items, nextPosition);
                Match nextUpstreamMatch = getUpstreamMatch(items, nextPosition + 1);
                if ((newCurrentMatch.isSuccess() && upstreamMatch.isSuccess() && !nextUpstreamMatch.isSuccess())
                        || (!newCurrentMatch.isSuccess() && upstreamMatch.isSuccess())) {
                    Match terminal = Match
                            .success(position, nextPosition)
                            .withGroups(groups.getItems());
                    return Pair.of(terminal, true);
                }

                // 3) The current pattern element is matched and there's a sibling that will not be matched if
                //    the cursor advances but it will be matched if the cursor remains
                Match nextSiblingMatch = getSiblingMatch(items, nextPosition + 1, Match.success(nextPosition + 1));
                if (newCurrentMatch.isSuccess() && siblingMatch.isSuccess() && !nextSiblingMatch.isSuccess()) {
                    Match terminal = Match
                            .success(position, nextPosition)
                            .and(siblingMatch)
                            .withGroups(groups.getItems());
                    return Pair.of(terminal, true);
                }
            }

            // Otherwise, retrieve the "ordinary" atomic result
            return Pair.of(findOne(items, nextPosition), false);
        }

        private Match getSiblingMatch(List<T> items, int position, Match defaultResult) {
            if (getNext() == null) {
                return defaultResult;
            }
            return getNext().findQuantified(items, position);
        }

        private Match getUpstreamMatch(List<T> items, int position) {
            if (getNext() != null || getUpstream() == null) {
                return Match.fail();
            }
            return getUpstream().findQuantified(items, position);
        }

        private boolean isExactNumberNeeded() {
            return min == max;
        }

    }
}
