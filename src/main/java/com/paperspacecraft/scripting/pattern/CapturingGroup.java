package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a RegExp-like capturing group inside a match
 */
public class CapturingGroup {

    private final int start;
    private final int end;

    /**
     * Instance constructor
     * @param start Starting position (inclusive) of the current group within the handled sequence
     * @param end   End position (exclusive) of the current group within the handled sequence
     */
    CapturingGroup(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Retrieves the starting position (inclusive) of the current group within the handled sequence
     * @return Int value
     */
    public int getStart() {
        return start;
    }

    /**
     * Retrieves the end position (exclusive) of the current group within the handled sequence
     * @return Int value
     */
    public int getEnd() {
        return end;
    }

    /**
     * Retrieves the size of the current group
     * @return Int value
     */
    public int getSize() {
        return getEnd() - getStart();
    }

    /**
     * Retrieves the sublist of the provided sequence containing matched items according to the position and size of the
     * current group.
     * <p>Note: This is a basic utility method that just applies the bounds of the current group to a given sequence. It
     * does not check whether it is the same sequence as the one used to create the group</p>
     * @param items The sequence to which the pattern was applied; an arbitrary-typed array
     * @return {@code List} object; might be an empty list if a wrong (not matching by size) sequence is provided
     */
    @Nonnull
    public <T> List<T> getHits(@Nonnull T[] items) {
        if (ArrayUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        return getHits(Arrays.asList(items));
    }

    /**
     * Retrieves the sublist of the provided sequence containing matched items according to the position and size of the
     * current group.
     * <p>Note: This is a basic utility method that just applies the bounds of the current group to a given sequence. It
     * does not check whether it is the same sequence as the one used to create the group</p>
     * @param items The sequence to which the pattern was applied; an arbitrary-typed list
     * @return {@code List} object; might be an empty list if a wrong (not matching by size) sequence is provided
     */
    @Nonnull
    public <T> List<T> getHits(@Nonnull List<T> items) {
        if (CollectionUtils.isEmpty(items) || end <= start || end > items.size()) {
            return Collections.emptyList();
        }
        return items.subList(start, end);
    }
}
