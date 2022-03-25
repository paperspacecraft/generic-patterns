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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Performs matching and replacing operations on a sequence of arbitrary objects using a {@link GenericPattern}
 * @param <T> Type of entities handled by this instance
 */
public class Matcher<T> implements MatchInfoProvider, GroupInfoProvider {

    private final GenericPattern<T> pattern;
    private List<T> items;

    private Match currentMatch;

    /**
     * Instance constructor
     * @param pattern {@link GenericPattern} instance
     * @param items   List of arbitrary entities that will be handled by this instance
     */
    Matcher(GenericPattern<T> pattern, List<T> items) {
        this.pattern = pattern;
        this.items = items instanceof ArrayList ? items : new ArrayList<>(items);
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
    public List<Group> getGroups() {
        return currentMatch != null ? currentMatch.getGroups() : Collections.emptyList();
    }

    /**
     * Resets the current {@link Matcher} so that the next {@link Matcher#find()} operation will start from the
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
        Match challenger = null;
        for (int i = position; i < items.size(); i++) {
            if (i > 0 && pattern.mustBeFirst()) {
                reset();
                return false;
            }
            Match match = pattern.getMatch(items, i);
            if (match.isSuccess()
                    && match.getSize() > 0
                    && isProperClosing(i, match.getSize())) {
                if (match.isComplete()) {
                    currentMatch = match;
                    return true;
                } else if (challenger == null){
                    challenger = match;
                }
            }
        }
        if (challenger != null) {
            currentMatch = challenger;
            return true;
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
     * Modifies and returns the handled sequence by replacing all subsequences that match the current pattern with the
     * provided value
     * @param replacement The value to use as the replacement. If a null or empty value is provided, the method works as
     *                    a deletion routine
     * @return Modified sequence; might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWith(@Nullable T replacement) {
        if (replacement == null) {
            return replaceWithList(Collections.emptyList());
        }
        return replaceWithList(Collections.singletonList(replacement));
    }

    /**
     * Modifies and returns the handled sequence by replacing all subsequences that match the current pattern with
     * values retrieved via the provided transform function
     * @param transform {@code Function} used to transform the matched sequence
     * @return Modified list; might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWith(@Nonnull Function<Match, T> transform) {
        return replaceWithList(match -> Collections.singletonList(transform.apply(match)));
    }

    /**
     * Modifies and returns the handled sequence by replacing all subsequences that match the current pattern with the
     * provided array value
     * @param replacement The value to use as the replacement. If a null or empty value is provided, the method works as
     *                    a deletion routine
     * @return Modified list; might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWithList(@Nullable T[] replacement) {
        if (replacement == null) {
            return replaceWithList(Collections.emptyList());
        }
        return replaceWithList(match -> Arrays.asList(replacement));
    }

    /**
     * Modifies and returns the handled sequence by replacing all subsequences that match the current pattern with the
     * provided list value
     * @param replacement The value to use as the replacement. If a null or empty value is provided, the method works as
     *                    a deletion routine
     * @return Modified list; might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWithList(@Nullable List<T> replacement) {
        return replaceWithList(match -> replacement);
    }

    /**
     * Modifies the handled sequence by replacing all subsequences that match the pattern with a new list retrieved from
     * the previous one via the provided modifier function
     * @param replacement {@code Function} used to transform the matched subsequence
     * @return Modified list; might be a non-null empty list
     */
    @Nonnull
    public List<T> replaceWithList(@Nonnull Function<Match, List<T>> replacement) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        reset();
        LinkedList<Match> results = new LinkedList<>();
        while (find()) {
            results.add(currentMatch);
        }

        Iterator<Match> resultsIterator = results.descendingIterator();
        if (items.stream().noneMatch(Objects::isNull)) {
            replaceOptimized(resultsIterator, replacement);
        } else {
            replaceDefault(resultsIterator, replacement);
        }

        return items;
    }

    private void replaceOptimized(Iterator<Match> resultsIterator, Function<Match, List<T>> replacement) {
        boolean hasNulls = false;

        while (resultsIterator.hasNext()) {
            Match current = resultsIterator.next();
            List<T> replacementList = replacement.apply(current);
            int insertionStart = current.getStart();
            int insertionEnd = current.getEnd();
            int insertionLengthDelta = (replacementList != null ? replacementList.size() : 0) - current.getSize();

            if (insertionLengthDelta <= 0 && replacementList == null) {
                for (int i = insertionStart; i < insertionEnd; i++) {
                    items.set(i, null);
                    hasNulls = true;
                }

            } else if (insertionLengthDelta <= 0) {
                int cursor = insertionStart;
                for (int i = 0; i < CollectionUtils.size(replacementList); i++) {
                    items.set(cursor++, replacementList.get(i));
                }
                for (int i = cursor; i < insertionEnd; i++) {
                    items.set(i, null);
                    hasNulls = true;
                }

            } else {
                assert replacementList != null;
                int cursor = 0;
                for (int i = insertionStart; i < insertionEnd; i++) {
                    items.set(i, replacementList.get(cursor++));
                }
                items.addAll(insertionEnd, replacementList.subList(cursor, replacementList.size()));
            }
        }
        if (hasNulls) {
            items = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    private void replaceDefault(Iterator<Match> resultsIterator, Function<Match, List<T>> replacement) {
        while (resultsIterator.hasNext()) {
            Match current = resultsIterator.next();
            List<T> matchedView = items.subList(current.getStart(), current.getEnd());
            List<T> replacementList = replacement.apply(current);
            matchedView.clear();
            if (CollectionUtils.isNotEmpty(replacementList)) {
                items.addAll(current.getStart(), replacementList);
            }
        }
    }

    /* ---------------
       Splitting logic
       --------------- */

    /**
     * Splits the handled sequence in chunks between the current pattern, then iterates the chunks. If the pattern is
     * not found, the whole sequence is returned
     * @return {@code Iterator} instance. On each successful iteration, a {@code List} containing the items that belong
     * to the current chunk is returned. The list might be empty if the matched pattern is situated at the very
     * beginning of the sequence
     */
    @Nonnull
    public Iterator<List<T>> split() {
        return new Iterator<List<T>>() {
            private int lastPosition = 0;

            @Override
            public boolean hasNext() {
                return CollectionUtils.isNotEmpty(items) && lastPosition < items.size();
            }

            @Override
            public List<T> next() {
                int newPosition;
                if (findAtPosition(lastPosition)) {
                    newPosition = getStart();
                } else {
                    newPosition = items.size();
                }
                List<T> result = items.subList(lastPosition, newPosition);
                lastPosition = newPosition + getSize();
                return result;
            }
        };
    }
}