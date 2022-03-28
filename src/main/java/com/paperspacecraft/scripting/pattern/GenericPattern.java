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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a RegExp-like expression applied to a sequence of arbitrary-typed entities. A pattern consists of one or
 * more predicates. If there are several predicates, patterns are organized in a chain
 * @param <T> Type of entities handled by this instance
 */
public abstract class GenericPattern<T> {

    /**
     * Link to a next sibling; used in processing pattern chains
     */
    private GenericPattern<T> next;

    /**
     * Link to a previous sibling; used to replace and dispose of a pattern in a chain without breaking the chain
     * consistency
     */
    private GenericPattern<T> previous;

    /**
     * Link to an "upstream" sibling (usually added to the training element in a capturing group so that the matching
     * could proceed "after" the group despite the last group's element not having a "next" sibling
     */
    private GenericPattern<T> upstream;

    /**
     * A reverse link to the pattern element that has its {@code upstream} value pointing to the current element. Used
     * to replace and dispose of a pattern in a chain without breaking the chain consistency
     */
    private GenericPattern<T> downstream;

    private boolean mustBeFirst;
    private boolean mustBeLast;
    private String tag;

    /* --------------
       Public methods
       -------------- */

    /**
     * Retrieves a {@link Matcher} instance used to extract entities that fit in the pattern from the given sequence
     * @param items The sequence to which the pattern is applied; an arbitrary-typed array
     * @return {@code Matcher} object
     */
    public final Matcher<T> matcher(@Nonnull T[] items) {
        return matcher(Arrays.asList(items));
    }

    /**
     * Retrieves a {@link Matcher} instance used to extract entities that fit in the pattern from the given sequence
     * @param items The sequence to which the pattern is applied; an arbitrary-typed collection
     * @return {@code Matcher} object
     */
    public final Matcher<T> matcher(@Nonnull List<T> items) {
        return new Matcher<>(this, items);
    }

    /* ----------------------------
       Non-public searching methods
       ---------------------------- */

    /**
     * Gets whether this pattern has been assigned the flag stating that the match must necessarily start from the first
     * element of a sequence
     * @return True or false
     */
    boolean mustBeFirst() {
        return mustBeFirst;
    }

    /**
     * Gets whether this pattern has been assigned the flag stating that the match must necessarily end with the
     * trailing element of a sequence
     * @return True or false
     */
    boolean mustBeLast() {
        return mustBeLast;
    }

    /**
     * Retrieves a {@link Match} object characterizing whether the given sequence fits in the patter if started from the
     * given position
     * @param items    The sequence to which the pattern is applied; an arbitrary-typed collection
     * @param position The position from which to start probing for the pattern
     * @return {@code MatchingResult} object
     */
    final Match getMatch(List<T> items, int position) {
        if (items == null || position >= items.size()) {
            return Match.fail();
        }
        return findQuantified(items, position);
    }

    /**
     * Called by {@link GenericPattern#getMatch(List, int)} to retrieve a match for the given entity sequence and
     * position that honors the possible quantifiers assigned to pattern elements
     * @param items    The sequence to which the pattern is applied; an arbitrary-typed list
     * @param position The position from which to start probing for the pattern
     * @return {@link Match} object
     */
    abstract Match findQuantified(List<T> items, int position);

    /* ------------------------------
       Non-public structuring methods
       ------------------------------ */

    /**
     * Retrieves the previous pattern element in the chain of patterns
     * @return {@link GenericPattern} instance
     */
    final GenericPattern<T> getPrevious() {
        return previous;
    }

    /**
     * Retrieves the next pattern element in the chain of patterns
     * @return {@link GenericPattern} instance
     */
    final GenericPattern<T> getNext() {
        return next;
    }

    /**
     * Retrieves the last pattern element in the pattern chain
     * @return {@link GenericPattern} instance
     */
    final GenericPattern<T> getLast() {
        return next != null ? next.getLast() : this;
    }

    /**
     * Retrieves the "upstream" pattern element - i.e., the element to which the match probing "returns" after the
     * current pattern chain is completed. Usually, this is the pattern element that goes immediately after a capturing
     * group, as related to this group
     * @return {@link GenericPattern} instance
     */
    final GenericPattern<T> getUpstream() {
        return upstream;
    }

    /**
     * Attaches the given pattern as the "next" element to the current one in a pattern chain
     * @param value {@link GenericPattern} object
     */
    void appendAsSibling(GenericPattern<T> value) {
        if (next == null) {
            next = value;
            value.previous = this;
        } else {
            next.appendAsSibling(value);
        }
    }

    /**
     * Replaces the "next" element of the current pattern with a new sibling pattern
     * @param value {@link GenericPattern} object
     */
    final void replaceSibling(GenericPattern<T> value) {
        if (next != null) {
            if (next.downstream != null) {
                next.downstream.upstream = value;
            }
            next.downstream = null;
            next.previous = null;
        }
        next = value;
        value.previous = this;
    }

    /**
     * Attaches the given pattern as the "upstream" element to the current one in a pattern chain
     * @param value {@link GenericPattern} object
     * @see GenericPattern#getUpstream()
     */
    final void appendAsUpstream(GenericPattern<T> value) {
        upstream = value;
        value.downstream = this;
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Retrieves an arbitrary string assigned to this instance. Used to retrieve additional info (vid. for debugging)
     * @return Nullable string value
     */
    String getTag() {
        return tag;
    }

    /**
     * Assigns an arbitrary string to this instance. Used to store additional info (vid. for debugging)
     * @param tag String value
     */
    void setTag(String tag) {
        this.tag = tag;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Starts a new {@link GenericPattern} with a builder
     * @param <U> Type of the entity this pattern will manage
     * @return Builder instance
     */
    public static <U> Starter<U> instance() {
        return new BuilderImpl<>();
    }

    /* ------------------
       Builder interfaces
       ------------------ */

    /**
     * Represents a pattern builder that is ready for a terminal operation, such as {@code build()} or creating a {@link
     * Matcher}
     * @param <T> Type of the entity this pattern will manage
     */
    public interface Finalizer<T> {

        /**
         * Completes this builder by producing a {@link GenericPattern} instance
         * @return {@code GenericPattern} object
         */
        GenericPattern<T> build();

        /**
         * Completes this builder by producing a {@link Matcher} instance
         * @param items The sequence to which the pattern will be applied; an arbitrary-typed array
         * @return {@code Matcher} object
         */
        default Matcher<T> matcher(T[] items) {
            return matcher(Arrays.asList(items));
        }

        /**
         * Completes this builder by producing a {@link Matcher} instance
         * @param items The sequence to which the pattern will be applied; an arbitrary-typed list
         * @return {@code Matcher} object
         */
        default Matcher<T> matcher(List<T> items) {
            return build().matcher(items);
        }
    }


    /**
     * Represents a pattern builder that accepts particular matching predicates or samples ("tokens") and also groups
     * @param <T> Type of the entity this pattern will manage
     */
    public interface Builder<T> extends Finalizer<T> {

        /**
         * Adds to the pattern a wildcard matching
         * @return Builder instance
         */
        default Token<T> any() {
            return token(arg -> true);
        }

        /**
         * Adds to the pattern matching sample
         * @param sample The object used as the sample to compare a sequence member to
         * @return Builder instance
         */
        default Token<T> token(T sample) {
            return token(arg -> Objects.equals(arg, sample));
        }

        /**
         * Adds to the pattern matching with predicate
         * @param predicate The predicate used to probe a sequence member to
         * @return Builder instance
         */
        default Token<T> token(Predicate<T> predicate) {
            SingleMatching<T> matching = new SingleMatching<>(predicate);
            return token(matching);
        }

        /**
         * Adds to the pattern a capturing group
         * @param builder A builder that creates a nested {@code GenericPattern} describing the group
         * @return Builder instance
         */
        default Token<T> token(GenericPattern.Builder<T> builder) {
            return token(builder.build());
        }

        /**
         * Adds to the pattern a capturing group
         * @param pattern A nested {@code GenericPattern} that describes the group
         * @return Builder instance
         */
        Token<T> token(GenericPattern<T> pattern);

        /**
         * Assigns the flag saying that the current pattern element must match the trailing sequence entry
         * @return Builder instance
         */
        Finalizer<T> ending();
    }

    /**
     * Represents a pattern builder that can be assigned the {@code beginning} flag
     * @param <T> Type of the entity this pattern will manage
     */
    public interface Starter<T> extends Builder<T> {

        /**
         * Assigns the flag saying that the current pattern element must match the leading sequence entry
         * @return Builder instance
         */
        Builder<T> beginning();
    }

    /**
     * Represents a pattern builder that can assign an alternative to a token that was added before the current
     * operation
     * @param <T> Type of the entity this pattern will manage
     */
    private interface Alternative<T> {

        /**
         * Adds an alternative value to a token
         * @param sample The object used as the sample to compare a sequence member to
         * @return Builder instance
         */
        default Token<T> or(T sample) {
            return or(arg -> Objects.equals(arg, sample));
        }

        /**
         * Adds an alternative value to a token
         * @param predicate The predicate used to probe a sequence member to
         * @return Builder instance
         */
        Token<T> or(Predicate<T> predicate);

        /**
         * Adds an alternative capturing group
         * @param builder A builder that creates a nested {@code GenericPattern} describing the group
         * @return Builder instance
         */
        default Token<T> or(GenericPattern.Builder<T> builder) {
            return or(builder.build());
        }

        /**
         * Adds to the pattern a capturing group
         * @param pattern A nested {@code GenericPattern} that describes the group
         * @return Builder instance
         */
        Token<T> or(GenericPattern<T> pattern);
    }


    /**
     * Represents a pattern builder that can assign a quantifier or else the "ending" flag to a token that was added
     * before the current operation
     * @param <T> Type of the entity this pattern will manage
     */
    public interface Token<T> extends Builder<T>, Alternative<T> {

        /**
         * Assign the "zero or one" quantifier (equivalent to the {@code ?} in RegExp
         * @return Builder instance
         */
        default Builder<T> zeroOrOne() {
            return count(0, 1);
        }

        /**
         * Assign the "zero or more" quantifier (equivalent to the {@code *} in RegExp
         * @return Builder instance
         */
        default Builder<T> zeroOrMore() {
            return count(0, Integer.MAX_VALUE);
        }

        /**
         * Assign the "one or more" quantifier (equivalent to the {@code +} in RegExp
         * @return Builder instance
         */
        default Builder<T> oneOrMore() {
            return count(1, Integer.MAX_VALUE);
        }

        /**
         * Assign the numeric quantifier (equivalent to the {@code {n}} in RegExp
         * @return Builder instance
         */
        default Builder<T> count(int value) {
            return count(value, value);
        }

        /**
         * Assign the numeric range quantifier (equivalent to the {@code {m,n}} in RegExp
         * @return Builder instance
         */
        Builder<T> count(int min, int max);

        /**
         * Assigns an arbitrary string to this instance. Used to store and retrieve additional info (vid. for
         * debugging)
         * @param value Nullable string
         * @return Builder instance
         */
        Token<T> tag(String value);
    }

    /* ---------------
       Builder classes
       --------------- */

    private static class BuilderImpl<T> implements Starter<T> {
        private GenericPattern<T> pattern;
        private boolean mustBeFirst;

        @Override
        public Builder<T> beginning() {
            mustBeFirst = true;
            return this;
        }

        @Override
        public Token<T> token(GenericPattern<T> pattern) {
            store(pattern);
            return new TokenImpl<>(this, pattern);
        }

        @Override
        public Finalizer<T> ending() {
            if (pattern == null) {
                return this;
            }
            pattern.getLast().mustBeLast = true;
            return this;
        }

        @Override
        public GenericPattern<T> build() {
            return new GroupMatching<>(pattern);
        }

        private void store(GenericPattern<T> matching) {
            if (pattern == null) {
                pattern = matching;
                pattern.mustBeFirst = mustBeFirst;
            } else {
                pattern.appendAsSibling(matching);
            }
        }

        private void storeAlternative(GenericPattern<T> matching) {
            if (pattern == null) {
                store(matching);
                return;
            }
            GenericPattern<T> last = pattern.getLast();
            if (last instanceof AlternativeMatching) {
                ((AlternativeMatching<T>) last).getAlternatives().add(matching);
                return;
            }
            GenericPattern<T> penultimate = pattern.getLast().getPrevious();
            if (penultimate != null) {
                penultimate.replaceSibling(new AlternativeMatching<>(last, matching));
            } else {
                pattern = new AlternativeMatching<>(pattern, matching);
            }
        }

        private void setTag(String value) {
            if (pattern == null) {
                return;
            }
            pattern.getLast().tag = value;
        }
    }

    private static class TokenImpl<T> implements Token<T> {

        private final BuilderImpl<T> commonBuilder;
        private GenericPattern<T> pattern;

        public TokenImpl(BuilderImpl<T> builder, GenericPattern<T> pattern) {
            this.commonBuilder = builder;
            this.pattern = pattern;
        }

        @Override
        public Token<T> token(GenericPattern<T> pattern) {
            return commonBuilder.token(pattern);
        }

        @Override
        public Token<T> or(Predicate<T> predicate) {
            return or(new SingleMatching<>(predicate));
        }

        @Override
        public Token<T> or(GenericPattern<T> pattern) {
            commonBuilder.storeAlternative(pattern);
            this.pattern = commonBuilder.pattern.getLast();
            return this;
        }

        @Override
        public Finalizer<T> ending() {
            if (pattern == null) {
                return this;
            }
            pattern.getLast().mustBeLast = true;
            return this;
        }

        @Override
        public Builder<T> count(int min, int max) {
            if (!(pattern instanceof QuantifiedMatching)) {
                throw new UnsupportedOperationException();
            }
            ((QuantifiedMatching<T>) pattern).setQuantifier(min, max);
            return commonBuilder;
        }

        @Override
        public GenericPattern<T> build() {
            return commonBuilder.build();
        }

        @Override
        public Token<T> tag(String value) {
            commonBuilder.setTag(value);
            return this;
        }
    }
}
