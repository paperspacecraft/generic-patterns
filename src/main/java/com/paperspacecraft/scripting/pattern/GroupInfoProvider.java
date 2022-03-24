package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the result of matching that exposes capturing groups
 * @see Group
 */
public interface GroupInfoProvider {

    /**
     * Retrieves the list of capturing groups containing matched items. The resulting value is either {@code null} if
     * there has been no match or a non-empty list. The first list item always represents the "complete" match. The
     * rest of the items represent particular capturing groups if there are any. Groups are sorted in order of their
     * start positions
     * @return {@code List} object. If no match is found, {@code null} is returned. Otherwise, a list containing at
     * least one group is returned
     */
    @Nullable
    List<Group> getGroups();

    /**
     * Retrieves the default capturing group
     * @return {@link Group} object; might be null
     */
    @Nullable
    default Group getGroup() {
        return CollectionUtils.isNotEmpty(getGroups()) ? getGroups().get(0) : null;
    }

    /**
     * Retrieves a capturing group by index
     * @param index Index of the group in the collection
     * @return {@link Group} object; might be null
     * @see Matcher#getGroups()
     */
    @Nullable
    default Group getGroup(int index) {
        return getGroups() != null && CollectionUtils.size(getGroups()) > index ? getGroups().get(index) : null;
    }
}
