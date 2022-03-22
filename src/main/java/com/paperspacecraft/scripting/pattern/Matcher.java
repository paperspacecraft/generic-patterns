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

import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Performs matching and replacing operations on a sequence of arbitrary objects using a {@link GenericPattern}
 * @param <T> Type of entities handled by this instance
 */
public class Matcher<T> implements MatchInfoProvider, GroupInfoProvider {

    private final GenericPattern<T> pattern;
    private final List<T> items;

    private Match currentMatch;

    /**
     * Instance constructor
     * @param pattern {@link GenericPattern} instance
     * @param items   List of arbitrary entities that will be handled by this instance
     */
    Matcher(GenericPattern<T> pattern, List<T> items) {
        this.pattern = pattern;
        this.items = items;
    }

    /* ------------------------------
       Common accessors and modifiers
       ------------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStart() {
        return currentMatch != null ? currentMatch.getStart() : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnd() {
        return currentMatch != null ? currentMatch.getEnd() : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public List<CapturingGroup> getGroups() {
        return currentMatch != null ? currentMatch.getGroups() : Collections.emptyList();
    }

    /**
     * Resets the current {@link Matcher}. It means that the next {@link Matcher#find()} operation will start from the
     * beginning of the handled sequence. Otherwise, it will continue from the end position of the last successful
     * match
     */
    public void reset() {
        currentMatch = null;
    }

    /* -------------
       Finding logic
       ------------- */

    /**
     * Finds the next match within the handled sequence. If there have already been matches, the search starts from the
     * end position of the last successful match. Otherwise, it starts from the beginning of the handled sequence
     * @return {@code True} if a new match is found; or else {@code false}
     */
    public boolean find() {
        return findAtPosition(Math.max(getEnd(), 0));
    }

    private boolean findAtPosition(int position) {
        if (CollectionUtils.isEmpty(items) || (position > 0 && pattern.mustBeFirst())) {
            return false;
        }
        for (int i = position; i < items.size(); i++) {
            if (i > 0 && pattern.mustBeFirst()) {
                reset();
                return false;
            }
            Match match = pattern.getMatch(items, i);
            if (match.isSuccess()
                    && match.getSize() > 0
                    && isProperClosing(i, match.getSize())) {
                currentMatch = match;
                return true;
            }
        }
        reset();
        return false;
    }

    private boolean isProperClosing(int position, int length) {
        return !pattern.mustBeLast() || (position + length) == items.size();
    }

    /* -----------------
       Replacement logic
       ----------------- */

    /**
     * Replaces all sequences within the handled entities list that match the current pattern with the provided value
     * @param replacement The value to use as the replacement. If a null or empty value is provided, the method works as
     *                    a deletion routine
     * @return Modified list (a new instance); might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWith(@Nullable T replacement) {
        if (replacement == null) {
            return replaceWithList(Collections.emptyList());
        }
        return replaceWithList(Collections.singletonList(replacement));
    }

    /**
     * Replaces all sequences within the handled entities list that match the current pattern with values retrieved via
     * the provided transform function
     * @param transform {@code Function} used to transform the matched sequence
     * @return Modified list (a new instance); might be a non-null empty list
     */
    public List<T> replaceWith(@Nonnull Function<Match, T> transform) {
        return replaceWithList(match -> Collections.singletonList(transform.apply(match)));
    }

    /**
     * Replaces all subsequences within the handled entities list that match the current pattern with the provided array
     * value
     * @param replacement The value to use as the replacement. If a null or empty value is provided, the method works as
     *                    a deletion routine
     * @return Modified list (a new instance); might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWithList(@Nullable T[] replacement) {
        if (replacement == null) {
            return replaceWithList(Collections.emptyList());
        }
        return replaceWithList(match -> Arrays.asList(replacement));
    }

    /**
     * Replaces all subsequences within the handled entities list that match the current pattern with the provided list
     * value
     * @param replacement The value to use as the replacement. If a null or empty value is provided, the method works as
     *                    a deletion routine
     * @return Modified list (a new instance); might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWithList(List<T> replacement) {
        return replaceWithList(match -> replacement);
    }

    /**
     * Replaces all subsequences within the handled entities list that match the current pattern with a new list
     * retrieved from the previous one via the provided modifier function
     * @param replacement {@code Function} used to transform the matched subsequence
     * @return Modified list (a new instance); might be a non-null empty list
     */
    public List<T> replaceWithList(@Nonnull Function<Match, List<T>> replacement) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        reset();
        List<T> result = new ArrayList<>(items);
        LinkedList<Match> results = new LinkedList<>();
        while (find()) {
            results.add(currentMatch);
        }

        Iterator<Match> resultsIterator = results.descendingIterator();
        while (resultsIterator.hasNext()) {
            Match current = resultsIterator.next();
            List<T> matchedView = result.subList(current.getStart(), current.getEnd());
            List<T> replacementList = replacement.apply(current);
            matchedView.clear();
            if (CollectionUtils.isNotEmpty(replacementList)) {
                result.addAll(current.getStart(), replacementList);
            }
        }
        return result;
    }
}