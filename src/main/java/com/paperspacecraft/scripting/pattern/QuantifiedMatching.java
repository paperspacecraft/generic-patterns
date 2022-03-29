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
         * @param items    The sequence of arbitrary-types entities to find a match in
         * @param position The position in the sequence from which to start the search
         * @param groups   {@code GroupCollection} accumulating object
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

            Match currentResult = findOne(items, nextPosition);

            if (!currentResult.isSuccess()) {
                return handleZeroLengthMatch(nextPosition);
            }

            while (currentResult.isSuccess()) {
                Pair<Match, Boolean> matchAndBreaking = handleMatchAndAdvance(currentResult);
                currentResult = matchAndBreaking.getLeft();
                boolean shouldBreak = matchAndBreaking.getRight();

                if (shouldBreak) {
                    return currentResult;
                }
            }

            if (isExactNumberNeeded() || matchCount < min) {
                return Match.fail();
            }

            return Match
                    .success(position, nextPosition)
                    .and(hasSibling() ? getNext().findQuantified(items, nextPosition) : Match.success(nextPosition))
                    .withGroups(groups.getItems());
        }

        private Match handleZeroLengthMatch(int position) {
            if (min > 0) {
                return Match.fail();
            }
            if (hasUpstream() && getUpstream().findQuantified(items, position).isSuccess()) {
                return Match.success(position);
            }
            if (hasSibling()) {
                return getNext().findQuantified(items, position);
            }
            return Match.incomplete(position);
        }

        private Pair<Match, Boolean> handleMatchAndAdvance(Match currentResult) {
            // Add the capturing groups for the current match if needed. (If the current pattern does not support
            // capturing groups, nothing will happen)
            groups.add(matchCount, nextPosition, nextPosition + currentResult.getSize(), currentResult);

            matchCount++;
            nextPosition = currentResult.getEnd();

            // We needed an exact number of matches or just reached the ceiling
            if (matchCount >= max) {
                Match fullMatch = currentResult.isComplete()
                        ? Match.success(position, nextPosition)
                        : Match.incomplete(position, nextPosition);

                fullMatch = fullMatch
                        .and(hasSibling() ? getNext().findQuantified(items, nextPosition) : Match.success(nextPosition))
                        .withGroups(groups.getItems());
                return Pair.of(fullMatch, true);

            // Else, we needed a non-exact number (corresponds to [*] or [+] in RegExp)
            } else if (matchCount >= min) {
                Pair<Match, Boolean> matchAndBreaking = handleNextMatch(position, nextPosition);
                if (matchAndBreaking != null) {
                    return matchAndBreaking;
                }
            }

            return Pair.of(findOne(items, nextPosition), false);
        }

        private Pair<Match, Boolean> handleNextMatch(int start, int current) {
            // By default, we perform a greedy search. Therefore, there can be several positions when we could be done
            // with the current match and move over to testing the next pattern. Some positions can be wrong (i.e. the
            // matching cannot proceed) even as the current pattern is satisfied. That's why we store the last position
            // that has proved proper.
            // If there isn't a position to move to the next pattern, or there isn't the next pattern at all, null is
            // returned from this method, and we just continue collecting matches from the current pattern in
            // Finder#handleMatchAndAdvance()
            int lastBestMatchPosition = -1;
            int currentMatchPosition = current;
            Match currentMatch = findOne(items, currentMatchPosition);
            while (currentMatch.isSuccess()) {
                int bestMatchCandidate = findContinuationPosition(currentMatchPosition);
                if (bestMatchCandidate > -1) {
                    lastBestMatchPosition = bestMatchCandidate;
                }
                currentMatch = findOne(items, currentMatchPosition++);
            }

            if (lastBestMatchPosition > -1) {
                if (hasSibling()) {
                    Match fullMatch = Match
                            .success(start, lastBestMatchPosition)
                            .and(getNext().findQuantified(items, lastBestMatchPosition))
                            .withGroups(groups.getItems());
                    return Pair.of(fullMatch, true);
                }
                Match fullMatch = Match
                        .success(start, lastBestMatchPosition)
                        .withGroups(groups.getItems());
                return Pair.of(fullMatch, true);
            }
            return null;
        }

        private int findContinuationPosition(int position) {
            if (hasUpstream() && getUpstream().findQuantified(items, position).isSuccess()) {
                return position;
            }
            int result = -1;
            if (!hasSibling()) {
                return result;
            }
            Match siblingMatch = getNext().findQuantified(items, position + 1);
            if (siblingMatch.isSuccess()) {
                result = position + 1;
            } else {
                siblingMatch = getNext().findQuantified(items, position);
                if (siblingMatch.isSuccess()) {
                    result = position;
                }
            }
            if (siblingMatch.isSuccess() && getNext().getUpstream() != null) {
                return getNext().getUpstream().findQuantified(items, siblingMatch.getEnd()).isSuccess() ? result : -1;
            }
            return result;
        }

        private boolean hasSibling() {
            return getNext() != null;
        }

        private boolean hasUpstream() {
            return !hasSibling() && getUpstream() != null;
        }

        private boolean isExactNumberNeeded() {
            return min == max;
        }
    }
}
