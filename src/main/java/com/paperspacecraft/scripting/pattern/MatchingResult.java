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
class MatchingResult {

    private static final MatchingResult SUCCESS_DEFAULT = MatchingResult.success(0);
    private static final MatchingResult FAIL = new MatchingResult(false, -1);

    private final boolean success;
    private final int size;

    private List<CapturingGroup> groups;

    /**
     * Instance constructor
     * @param success Flag indicating whether this is a successful matching
     * @param size Size of the matching
     */
    private MatchingResult(boolean success, int size) {
        this.success = success;
        this.size = size;
    }

    /* ---------
       Accessors
       --------- */

    /**
     * Gets whether the matching was found
     * @return True or false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the length of the matched sub-sequence
     * @return True or false
     */
    public int getSize() {
        return size;
    }

    /**
     * Retrieves the captured groups associated with the current matching
     * @return {@code List} object; might be null
     */
    @Nullable
    public List<CapturingGroup> getGroups() {
        return groups;
    }

    /**
     * Assigns the list of capturing groups to this instance in a builder-like manner
     * @param value {@code List} object
     * @return This instance
     */
    public MatchingResult withGroups(List<CapturingGroup> value) {
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
     * Assigns another {@link MatchingResult} into the current instance in a builder-like manner
     * @param other {@code MatchingResult} object
     * @return This instance
     */
    public MatchingResult and(MatchingResult other) {
        if (other == null) {
            return this;
        }
        if (success && other.success) {
            MatchingResult result = success(size + other.size);
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
     * Retrieves the zero-sized successful matching result
     * @return {@link MatchingResult} instance
     */
    public static MatchingResult success() {
        return SUCCESS_DEFAULT;
    }

    /**
     * Retrieves a successful matching result of the provided size
     * @param size The reported size of the matching result
     * @return {@link MatchingResult} instance
     */
    public static MatchingResult success(int size) {
        return new MatchingResult(true, size);
    }

    /**
     * Retrieves a non-successful matching result
     * @return {@link MatchingResult} instance
     */
    public static MatchingResult fail() {
        return FAIL;
    }
}
