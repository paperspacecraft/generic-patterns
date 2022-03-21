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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a singular matching operation
 */
public class Match {

    private static final Match FAIL = new Match(false, -1, -1);

    private final boolean success;
    private final int start;
    private final int end;

    private List<CapturingGroup> groups;

    /**
     * Instance constructor
     * @param success Flag indicating whether this is a successful match
     * @param start   Start position of the match
     * @param end     End position of the match
     */
    private Match(boolean success, int start, int end) {
        this.success = success;
        this.start = start;
        this.end = end;
    }

    /* ---------
       Accessors
       --------- */

    /**
     * Gets whether the match was found
     * @return True or false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Retrieves the starting position of this match
     * @return Int value
     */
    public int getStart() {
        return start;
    }

    /**
     * Retrieves the end position of this match
     * @return Int value
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gets the length of the matched sub-sequence
     * @return True or false
     */
    public int getSize() {
        return end - start;
    }

    /**
     * Retrieves the capturing groups associated with the current match
     * @return {@code List} object; might be null
     */
    @Nullable
    public List<CapturingGroup> getGroups() {
        return groups;
    }

    /**
     * Retrieves a capturing group by index
     * @param index Index of the group in the collection
     * @return {@link CapturingGroup} object; might be null
     */
    @Nullable
    public CapturingGroup getGroup(int index) {
        return groups != null && index >= 0 && index < groups.size() ? groups.get(index) : null;
    }

    /**
     * Retrieves the sublist containing matched items
     * @return {@code List} object; null can be returned if no match is found
     */
    @Nullable
    public <T> List<T> getHits(List<T> items) {
        if (getStart() < 0 || getSize() <= 0 || CollectionUtils.isEmpty(items)) {
            return null;
        }
        return items.subList(start, end);
    }

    /**
     * Assigns the list of capturing groups to this instance in a builder-like manner
     * @param value {@code List} object
     * @return This instance
     */
    Match withGroups(List<CapturingGroup> value) {
        if (!isSuccess() || CollectionUtils.isEmpty(value)) {
            return this;
        }
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.addAll(value);
        return this;
    }

    /**
     * Assigns another {@link Match} into the current instance in a builder-like manner
     * @param other {@code Match} object
     * @return This instance
     */
    Match and(Match other) {
        if (other == null) {
            return this;
        }
        if (success && other.success) {
            Match result = success(Math.min(this.start, other.start), Math.max(this.end, other.end));
            if (groups != null) {
                result.groups = groups;
            }
            if (other.groups != null && result.groups != null) {
                result.groups.addAll(other.groups);
            } else if (other.groups != null) {
                result.groups = other.groups;
            }
            return result;
        }
        return fail();
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Retrieves the zero-sized successful match
     * @param start Start position of the match
     * @return {@link Match} instance
     */
    static Match success(int start) {
        return new Match(true, start, start);
    }

    /**
     * Retrieves a successful match starting as ths given position with the provided size
     * @param start Start position of the match
     * @param end   End position of the match
     * @return {@link Match} instance
     */
    static Match success(int start, int end) {
        return new Match(true, start, end);
    }

    /**
     * Retrieves a non-successful match
     * @return {@link Match} instance
     */
    static Match fail() {
        return FAIL;
    }
}
