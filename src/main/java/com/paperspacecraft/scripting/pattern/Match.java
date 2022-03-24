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
public class Match implements MatchInfoProvider, GroupInfoProvider {

    private static final Match FAIL = new Match(false, -1, -1);

    private final boolean success;
    private final int start;
    private final int end;

    private List<Group> groups;

    /**
     * Instance constructor
     * @param success Flag indicating whether this is a successful match
     * @param start   The start position of the match
     * @param end     The end position of the match
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
     * {@inheritDoc}
     */
    @Override
    public int getStart() {
        return start;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEnd() {
        return end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public List<Group> getGroups() {
        return groups;
    }

    /* ---------------------------
       Utility composition methods
       --------------------------- */

    /**
     * Assigns the list of capturing groups to this instance in a builder-like manner
     * @param value {@code List} object
     * @return This instance
     */
    Match withGroups(List<Group> value) {
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
     * @param start The start position of the match
     * @return {@link Match} instance
     */
    static Match success(int start) {
        return new Match(true, start, start);
    }

    /**
     * Retrieves a successful match starting as the given position with the provided size
     * @param start The start position of the match
     * @param end   The end position of the match
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
