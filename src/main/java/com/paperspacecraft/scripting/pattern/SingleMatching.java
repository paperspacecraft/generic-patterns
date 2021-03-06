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
import java.util.function.Predicate;

/**
 * Extends {@link QuantifiedMatching} for performing an atomic test with a particular pattern element
 * @param <T> Type of entities handled by this instance
 */
class SingleMatching<T> extends QuantifiedMatching<T> {

    private final Predicate<T> predicate;

    /**
     * Instance constructor
     * @param predicate The {@code Predicate} object used to test entries of a sequence
     */
    public SingleMatching(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Match findOne(List<T> items, int position) {
        return position < items.size() && items.get(position) != null && predicate.test(items.get(position))
            ? Match.success(position, position + 1)
            : Match.fail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GroupCollection getGroups() {
        return new GroupCollection(false);
    }
}
