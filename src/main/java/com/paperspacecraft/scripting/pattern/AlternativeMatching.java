package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Extends {@link QuantifiedMatching} for performing tests against a set of alternative patterns
 * @param <T> Type of entities handled by this instance
 */
class AlternativeMatching<T> extends QuantifiedMatching<T> {

    private final LinkedList<GenericPattern<T>> alternatives;

    public AlternativeMatching(GenericPattern<T> left, GenericPattern<T> right) {
        alternatives = new LinkedList<>();
        alternatives.add(left);
        alternatives.add(right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void appendAsSibling(GenericPattern<T> value) {
        super.appendAsSibling(value);
        if (alternatives.getLast() instanceof GroupMatching) {
            ((GroupMatching<T>) alternatives.getLast()).getEntryPoint().getLast().appendAsUpstream(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Match findOne(List<T> items, int position) {
        if (CollectionUtils.isEmpty(alternatives)) {
            return Match.fail();
        }
        for (GenericPattern<T> alternative : alternatives) {
            if (! (alternative instanceof QuantifiedMatching)) {
                continue;
            }
            Match result = ((QuantifiedMatching<T>) alternative).findOne(items, position);
            if (result.isSuccess()) {
                return result;
            }
        }
        return Match.fail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GroupCollection getGroups() {
        return new GroupCollection(alternatives.stream().anyMatch(alt -> alt instanceof GroupMatching));
    }

    /**
     * Retrieves the alternative patterns associated with this instance
     * @return {@code List} object; might be an empty list
     */
    List<GenericPattern<T>> getAlternatives() {
        return alternatives;
    }
}
