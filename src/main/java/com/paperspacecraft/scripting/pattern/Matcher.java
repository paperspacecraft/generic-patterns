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
 * Performs matching operations on a sequence of arbitrary objects using a {@link GenericPattern}
 * @param <T> Type of entities handled by this instance
 */
public class Matcher<T> {

    private final GenericPattern<T> pattern;
    private final List<T> items;

    private int start = -1;
    private int end = -1;
    private List<CapturingGroup> groups;

    /**
     * Instance constructor
     * @param pattern {@link GenericPattern} instance
     * @param items   List of arbitrary entities that will be handled by this instance
     */
    Matcher(GenericPattern<T> pattern, List<T> items) {
        this.pattern = pattern;
        this.items = items;
    }

    /**
     * Retrieves the starting position (inclusive) of the matching found within the handled sequence of entities
     * @return Int value. If no matching is found, {@code -1} is returned
     */
    public int getStart() {
        return start;
    }

    /**
     * Retrieves the end position (exclusive) of the matching found within the handled sequence of entities
     * @return Int value. If no matching is found, {@code -1} is returned
     */
    public int getEnd() {
        return end;
    }

    /**
     * Retrieves the size (number of entities) of the matching found within the handled sequence
     * @return Int value. If no matching is found, {@code 0} is returned
     */
    public int getSize() {
        return end - start;
    }

    /**
     * Retrieves the sublist of the handled sequence containing matched items
     * @return {@code List} object; an empty list if no matching is found
     */
    @Nonnull
    public List<T> getHits() {
        if (start == end || CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        return items.subList(start, end);
    }

    /**
     * Retrieves the list of capturing groups containing matched items. The resulting value is either {@code null} if
     * there has been no match or a non-empty list. The first list item always represents the "complete" matching. The
     * rest of the items represent particular capturing groups if there are any. Groups are sorted in order of their
     * start positions
     * @return {@code List} object. If no matching is found, {@code null} is returned. Otherwise, a list containing at
     * least one group is returned
     */
    @Nullable
    public List<CapturingGroup> getGroups() {
        return groups;
    }

    /**
     * Resets the current {@link Matcher}. It means that the next {@link Matcher#find()} operation will start from the
     * beginning of the handled sequence. Otherwise, it will continue from the end position of the last successful
     * match
     */
    public void reset() {
        start = -1;
        end = -1;
    }

    /**
     * Finds the next matching within the handled sequence. If there have already been matches, the search starts from
     * the end position of the last successful matching. Otherwise, it starts from the beginning of the handled sequence
     * @return {@code True} if a new matching is found; or else {@code false}
     */
    public boolean find() {
        return findAtPosition(Math.max(end, 0));
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
            MatchingResult matchingResult = pattern.getResult(items, i);
            if (matchingResult.isSuccess()
                    && matchingResult.getSize() > 0
                    && isProperClosing(i, matchingResult.getSize())) {
                start = i;
                end = start + matchingResult.getSize();
                groups = matchingResult.getGroups();
                return true;
            }
        }
        reset();
        return false;
    }

    private boolean isProperClosing(int position, int length) {
        return !pattern.mustBeLast() || (position + length) == items.size();
    }
}
