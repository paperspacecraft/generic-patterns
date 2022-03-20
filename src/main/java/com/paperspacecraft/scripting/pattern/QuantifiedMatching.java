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
     * Assigns the target matching numbers
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
    MatchingResult findQuantified(List<T> items, int position) {
        return new Finder(items, position, getCapturingGroups()).find();
    }

    /**
     * Retrieve the first matching (atomic) result for the given entity sequence
     * @param items    The sequence to which the pattern is applied; an arbitrary-typed list
     * @param position The position from which to start probing for the pattern
     * @return {@code MatchingResult} object
     */
    abstract MatchingResult findOne(List<T> items, int position);

    /* ------------------------
       Matching extension logic
       ------------------------ */

    /**
     * Initializes a {@link CapturingGroupCollection} in either enabled or disabled state
     * @return {@code CapturingGroupCollection} object
     */
    abstract CapturingGroupCollection getCapturingGroups();

    private MatchingResult getSiblingMatching(List<T> items, int position, MatchingResult defaultResult) {
        if (getNext() == null) {
            return defaultResult;
        }
        return getNext().findQuantified(items, position);
    }

    private MatchingResult getUpstreamMatching(List<T> items, int position) {
        if (getNext() != null || getUpstream() == null) {
            return MatchingResult.fail();
        }
        return getUpstream().findQuantified(items, position);
    }

    /* ----------------
       Quantifier logic
       ---------------- */

    private boolean isExactNumberNeeded() {
        return min == max;
    }

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
        private final CapturingGroupCollection capturingGroups;

        private int matchCount;
        private int cursor;

        /**
         * Instance constructor
         * @param items           The sequence of arbitrary-types entities to find the matching in
         * @param position        The position in the sequence from which to start the search
         * @param capturingGroups {@code CapturingGroupCollection} accumulating object
         */
        public Finder(List<T> items, int position, CapturingGroupCollection capturingGroups) {
            this.items = items;
            this.position = position;
            this.capturingGroups = capturingGroups;
        }

        /**
         * Finds the sequence of atomic matches per separate pattern elements in a loop
         * @return {@code MatchingResult} object
         */
        public MatchingResult find() {
            matchCount = 0;
            cursor = position;

            // Process the first occurrence
            MatchingResult currentResult = findOne(items, cursor);

            // If the current result is no match but the quantifier allows zero matches, and there's an upstream (i.e.,
            // we are within the terminal member of a capturing group), we must check whether the check can continue
            // even without a match in the current member.
            // Note that we don't create a capturing group for this case
            if (!currentResult.isSuccess() && min == 0 && getUpstreamMatching(items, position).isSuccess()) {
                return MatchingResult.success();
            }

            // If the current result is no match but the quantifier allows zero matches, and there's a sibling pattern
            // element that matches the current item, we report success based on the sibling math
            if (!currentResult.isSuccess() && min == 0) {
                MatchingResult siblingResult = getSiblingMatching(items, position, MatchingResult.fail());
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
                    return getSiblingMatching(items, cursor, MatchingResult.success());
                }
                return MatchingResult.fail();
            }

            // Now there's a match. Process all the available occurrences. We do this in a loop. Counters are
            // incremented at the loop start to account for the match that has already been received
            while (currentResult.isSuccess()) {
                Pair<MatchingResult, Boolean> resultAndBreak = processAndAdvance(currentResult);
                if (resultAndBreak.getRight()) {
                    return resultAndBreak.getLeft();
                }
                currentResult = resultAndBreak.getLeft();
            }

            // Now the matching search is over. If the exact result is needed, we are here because we haven't reached it.
            // Therefore, fail
            if (isExactNumberNeeded() || matchCount < min) {
                return MatchingResult.fail();
            }
            // Otherwise, report local success and pass it on to the next chain link
            return MatchingResult
                    .success(cursor - position)
                    .and(getSiblingMatching(items, cursor, MatchingResult.success()))
                    .withGroups(capturingGroups.getItems());
        }

        private Pair<MatchingResult, Boolean> processAndAdvance(MatchingResult currentResult) {
            // Add the capturing groups for the current match if needed. (If the current pattern does not support
            // capturing groups, nothing will occur)
            capturingGroups.add(matchCount, cursor, cursor + currentResult.getSize(), currentResult);

            matchCount++;
            cursor += currentResult.getSize();

            if (matchCount == max) {
                MatchingResult terminal = MatchingResult
                        .success(cursor - position)
                        .and(getSiblingMatching(items, cursor, MatchingResult.success(0)))
                        .withGroups(capturingGroups.getItems());
                return Pair.of(terminal, true);

            } else if (!isExactNumberNeeded() && matchCount >= min) {
                // There are three reasons why we can break away from the loop early:
                // 1) the current pattern element is not matched but the sibling is matched;
                // 2) the current pattern element is matched and there's a sibling that will not be matched if the
                //     cursor advances but it will be matched if the cursor remains
                MatchingResult newCurrentMatching = findOne(items, cursor);
                MatchingResult siblingMatching = getSiblingMatching(items, cursor, MatchingResult.fail());
                MatchingResult nextSiblingMatching = getSiblingMatching(items, cursor + 1, MatchingResult.success());
                boolean canReturnNow = (!newCurrentMatching.isSuccess() && siblingMatching.isSuccess())
                        || (newCurrentMatching.isSuccess() && siblingMatching.isSuccess() && !nextSiblingMatching.isSuccess());

                if (canReturnNow) {
                    MatchingResult terminal = MatchingResult
                            .success(cursor - position)
                            .and(siblingMatching)
                            .withGroups(capturingGroups.getItems());
                    return Pair.of(terminal, true);
                }
                // 3) The third reason is that there's the matching upstream
                if (!newCurrentMatching.isSuccess() && getUpstreamMatching(items, cursor).isSuccess()) {
                    MatchingResult terminal = MatchingResult
                            .success(cursor - position)
                            .withGroups(capturingGroups.getItems());
                    return Pair.of(terminal, true);
                }
            }

            // Otherwise, retrieve the "ordinary" atomic result
            return Pair.of(findOne(items, cursor), false);
        }
    }
}
