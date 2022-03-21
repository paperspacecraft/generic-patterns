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

    private final GenericPattern<T> parent;

    /**
     * Instance constructor
     * @param parent The {@link GenericPattern} to which this group belongs
     */
    public GroupMatching(GenericPattern<T> parent) {
        this.parent = parent;
    }

    /**
     * Retrieves the {@link GenericPattern} to which this group belongs
     * @return {@code GenericPatter} object
     */
    GenericPattern<T> getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void appendAsSibling(GenericPattern<T> value) {
        super.appendAsSibling(value);
        parent.getLast().appendAsUpstream(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Match findOne(List<T> items, int position) {
        return parent.findQuantified(items, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    CapturingGroupCollection getCapturingGroups() {
        return new CapturingGroupCollection(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean mustBeFirst() {
        return super.mustBeFirst() || parent.mustBeFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean mustBeLast() {
        return super.mustBeLast() || parent.getLast().mustBeLast();
    }
}
