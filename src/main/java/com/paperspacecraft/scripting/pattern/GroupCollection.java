package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An accumulator of capturing groups found while performing a particular matching routine
 */
class GroupCollection {

    private final boolean enabled;

    private List<Group> groups;

    private int previousStartPosition;
    private int previousEndPosition;
    private boolean optimized;

    /**
     * Instance constructor
     * @param enabled A flag specifying whether the capturing groups will be collected or skipped
     */
    public GroupCollection(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Adds a new capturing group to the collection. Does nothing if the {@code enabled} flag was set to {@code false}
     * @param iteration     Numeric value that specifies whether this operation is performed for the first type in the
     *                      ongoing matching routine or it is a repetitive run. This is important to know because if
     *                      several group instances originate from the same pattern element (e.g., in RegExp {@code
     *                      /(ab)+/.test("ababab")} there are effectively three groups), we only need to store the last
     *                      one, per the RegExp standard
     * @param startPosition The start position (inclusive) of the group in the given sequence
     * @param endPosition   The end position (exclusive) of the group in the given sequence
     * @param match         {@link Match} object used as the source of secondary/nested capturing groups
     */
    public void add(int iteration, int startPosition, int endPosition, Match match) {
        if (!enabled) {
            return;
        }
        if (groups == null) {
            groups = new ArrayList<>();
        }
        if (iteration > 0) {
            // A repeated capturing group should only capture the last iteration. That's why we remove the result of
            // the previous iteration. This is the way RegExp works
            groups
                    .stream()
                    .filter(group -> group.getStart() == previousStartPosition && group.getEnd() == previousEndPosition)
                    .findFirst()
                    .ifPresent(groups::remove);
        }
        previousStartPosition = startPosition;
        previousEndPosition = endPosition;
        groups.add(new Group(startPosition, endPosition));
        if (CollectionUtils.isNotEmpty(match.getGroups())) {
            groups.addAll(match.getGroups());
        }
    }

    /**
     * Retrieves the capturing groups stored in this collection. Before outputting, the list is sorted in order of the
     * starting positions
     * @return Nullable {@code List} object
     */
    @Nullable
    public List<Group> getItems() {
        optimize();
        return groups;
    }

    private void optimize() {
        if (CollectionUtils.isEmpty(groups) || optimized) {
            return;
        }
        groups.sort(Comparator.comparingInt(Group::getStart));
        optimized = true;
    }
}
