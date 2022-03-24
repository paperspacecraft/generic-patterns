package com.paperspacecraft.scripting.pattern;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SplitTest {

    private static final List<Integer> SEQUENCE = Arrays.asList(5, 11, 7, 15, 21, 7, 11, 32);

    @Test
    public void shouldSplitByMissingPattern() {
        Iterator<List<Integer>> iterator = GenericPattern
                .<Integer>instance()
                .token(18)
                .matcher(SEQUENCE)
                .split();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(8, iterator.next().size());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldSplitByPattern() {
        Iterator<List<Integer>> iterator = GenericPattern
                .<Integer>instance()
                .token(7)
                .matcher(SEQUENCE)
                .split();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(2, iterator.next().size());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(2, iterator.next().size());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(2, iterator.next().size());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldSplitByLengthyPattern() {
        Iterator<List<Integer>> iterator = GenericPattern
                .<Integer>instance()
                .token(t -> t == 11 || t == 7).oneOrMore()
                .matcher(SEQUENCE)
                .split();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(1, iterator.next().size());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(2, iterator.next().size());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(1, iterator.next().size());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldSplitByMatchingAllPattern() {
        Iterator<List<Integer>> iterator = GenericPattern
                .<Integer>instance()
                .any()
                .matcher(SEQUENCE)
                .split();
        for (int i = 0; i < SEQUENCE.size(); i++) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(0, iterator.next().size());
        }
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldHonorPatternsAtEnds1() {
        Iterator<List<Integer>> iterator = GenericPattern
                .<Integer>instance()
                .token(t -> t == 5 || t == 11).oneOrMore()
                .matcher(SEQUENCE)
                .split();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(0, iterator.next().size());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(4, iterator.next().size());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(1, iterator.next().size());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldHonorPatternsAtEnds2() {
        Iterator<List<Integer>> iterator = GenericPattern
                .<Integer>instance()
                .token(32)
                .matcher(SEQUENCE)
                .split();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(7, iterator.next().size());
        Assert.assertFalse(iterator.hasNext());
    }
}

