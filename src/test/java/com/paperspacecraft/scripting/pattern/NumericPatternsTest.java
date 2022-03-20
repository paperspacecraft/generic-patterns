package com.paperspacecraft.scripting.pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NumericPatternsTest {

    private static final Integer[] SEQUENCE = ArrayUtils.toObject(new int[] {2, 15, 42, 42, 15});

    @Test
    public void shouldMatchSequence() {
        GenericPattern<Integer> pattern = GenericPattern
                .<Integer>instance()
                .token(15)
                .token(42).oneOrMore()
                .build();
        Matcher<Integer> matcher = pattern.matcher(SEQUENCE);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(1, matcher.getStart());
        Assert.assertEquals(3, matcher.getSize());
    }

    @Test
    public void shouldMatchStartOfSequence() {
        GenericPattern<Integer> matchingPattern = GenericPattern
                .<Integer>instance()
                .beginning()
                .token(2)
                .token(15)
                .token(42)
                .build();
        Assert.assertTrue(matchingPattern.matcher(SEQUENCE).find());

        GenericPattern<Integer> nonMatchingPattern = GenericPattern
                .<Integer>instance()
                .beginning()
                .token(42)
                .token(42)
                .token(15)
                .ending()
                .build();
        Assert.assertFalse(nonMatchingPattern.matcher(SEQUENCE).find());
    }

    @Test
    public void shouldMatchEndOfSequence() {
        GenericPattern<Integer> matchingPattern = GenericPattern
                .<Integer>instance()
                .token(42)
                .token(42)
                .token(15)
                .ending()
                .build();
        Assert.assertTrue(matchingPattern.matcher(SEQUENCE).find());

        GenericPattern<Integer> nonMatchingPattern = GenericPattern
                .<Integer>instance()
                .beginning()
                .token(15)
                .token(42)
                .token(42)
                .build();
        Assert.assertFalse(nonMatchingPattern.matcher(SEQUENCE).find());
    }

    @Test
    public void shouldProcessSerialMatches() {
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(num -> num % 10 == 2)
                .matcher(SEQUENCE);

        List<Integer> hits = new ArrayList<>();
        while (matcher.find()) {
            Assert.assertNotNull(matcher.getGroups());
            int hit = matcher.getGroups().get(0).getHits(SEQUENCE).get(0);
            hits.add(hit);
        }
        Assert.assertEquals(3, hits.size());
        Assert.assertEquals(2, (int) hits.get(0));
        Assert.assertEquals(42, (int) hits.get(1));
        Assert.assertEquals(42, (int) hits.get(2));
    }
}
