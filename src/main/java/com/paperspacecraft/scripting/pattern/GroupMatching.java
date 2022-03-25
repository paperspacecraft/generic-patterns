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

import java.util.List;

/**
 * Extends {@link QuantifiedMatching} for performing tests with a sequence of pattern elements that form up a capturing
 * group
 * @param <T> Type of entities handled by this instance
 */
class GroupMatching<T> extends QuantifiedMatching<T> {

    private final GenericPattern<T> entryPoint;

    /**
     * Instance constructor
     * @param entryPoint The {@link GenericPattern} to which this group belongs
     */
    public GroupMatching(GenericPattern<T> entryPoint) {
        this.entryPoint = entryPoint;
    }

    /**
     * Retrieves the {@link GenericPattern} to which this group belongs
     * @return {@code GenericPatter} object
     */
    GenericPattern<T> getEntryPoint() {
        return entryPoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void appendAsSibling(GenericPattern<T> value) {
        super.appendAsSibling(value);
        if (entryPoint.getLast().getUpstream() == null) {
            entryPoint.getLast().appendAsUpstream(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Match findOne(List<T> items, int position) {
        return entryPoint.findQuantified(items, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GroupCollection getGroups() {
        return new GroupCollection(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean mustBeFirst() {
        return super.mustBeFirst() || entryPoint.mustBeFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean mustBeLast() {
        return super.mustBeLast() || entryPoint.getLast().mustBeLast();
    }
}
