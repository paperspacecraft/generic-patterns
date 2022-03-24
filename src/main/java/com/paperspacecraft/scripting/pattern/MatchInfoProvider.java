package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the result of matching that exposes a position in the handled sequence as well as the captured items
 */
public interface MatchInfoProvider {

    /**
     * Retrieves the starting position of this match
     * @return Int value
     */
    int getStart();

    /**
     * Retrieves the end position of this match
     * @return Int value
     */
    int getEnd();

    /**
     * Gets the length of the matched sequence
     * @return True or false
     */
    default int getSize() {
        return getEnd() - getStart();
    }

    /**
     * Retrieves the segment of the provided sequence containing matched items.
     * <p>Note: This is a basic utility that just applies the bounds to a passed sequence. It does not check whether it
     * is the same sequence as the one used to retrieve the match</p>
     * @param items The sequence to which the pattern was applied; an arbitrary-typed array
     * @return {@code List} object; might be an empty list if a wrong (not matching by size) sequence is provided
     */
    @Nullable
    default <T> List<T> getHits(T[] items) {
        if (ArrayUtils.isEmpty(items)) {
            return null;
        }
        return getHits(Arrays.asList(items));
    }

    /**
     * Retrieves the sublist of the provided sequence containing the matched items.
     * <p>Note: This is a basic utility that just applies the bounds to a passed sequence. It does not check whether it
     * is the same sequence as the one used to retrieve the match</p>
     * @param items The sequence to which the pattern was applied; an arbitrary-typed list
     * @return {@code List} object; might be null if a wrong sequence is provided
     */
    @Nullable
    default <T> List<T> getHits(List<T> items) {
        if (getStart() < 0 || getSize() <= 0 || (getStart() + getSize()) > CollectionUtils.size(items)) {
            return null;
        }
        return items.subList(getStart(), getEnd());
    }

    /**
     * Retrieves the array containing matched items from the provided sequence.
     * <p>Note: This is a basic utility that just applies the bounds to a passed sequence. It does not check whether it
     * is the same sequence as the one used to retrieve the match</p>
     * @param items The sequence to which the pattern was applied; an arbitrary-typed list
     * @return {@code List} object; might be null if a wrong sequence is provided
     */
    default <T> T[] getHitArray(T[] items) {
        if (getStart() < 0 || getSize() <= 0 || (getStart() + getSize()) > ArrayUtils.getLength(items)) {
            return null;
        }
        return ArrayUtils.subarray(items, getStart(), getEnd());
    }

    /**
     * Retrieves the array containing matched items from the provided sequence.
     * <p>Note: This is a basic utility that just applies the bounds to a passed sequence. It does not check whether it
     * is the same sequence as the one used to retrieve the match</p>
     * @param items    The sequence to which the pattern was applied; an arbitrary-typed list
     * @return {@code T}-typed array object; might be null if a wrong sequence is provided
     */
    @Nullable
    default <T> T[] getHitArray(List<T> items) {
        return getHitArray(items, null);
    }

    /**
     * Retrieves the array containing matched items from the provided sequence.
     * <p>Note: This is a basic utility that just applies the bounds to a passed sequence. It does not check whether it
     * is the same sequence as the one used to retrieve the match</p>
     * @param items    The sequence to which the pattern was applied; an arbitrary-typed list
     * @param itemType {@code Class} reference pointing at the desired item type (can be, e.g., an ancestral class of
     *                 the actual items' classes). If not specified, the class of the first item in the sequence will be
     *                 used
     * @return {@code T}-typed array object; might be null if a wrong sequence is provided
     */
    @Nullable
    default <T> T[] getHitArray(List<T> items, Class<?> itemType) {
        if (getStart() < 0 || getSize() <= 0 || (getStart() + getSize()) > CollectionUtils.size(items)) {
            return null;
        }
        Class<?> effectiveItemType = itemType != null ? itemType : items.get(0).getClass();
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(effectiveItemType, getSize());
        for (int i = getStart(); i < getStart() + result.length; i++) {
            result[i - getStart()] = items.get(i);
        }
        return result;
    }
}
