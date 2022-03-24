package com.paperspacecraft.scripting.pattern;

/**
 * Represents a RegExp-like capturing group inside a match
 */
public class Group implements MatchInfoProvider {

    private final int start;
    private final int end;

    /**
     * Instance constructor
     * @param start Starting position (inclusive) of the current group within the handled sequence
     * @param end   End position (exclusive) of the current group within the handled sequence
     */
    Group(int start, int end) {
        this.start = start;
        this.end = end;
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
    public int getEnd() {
        return end;
    }
}
