package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MatcherTest {

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
    public void shouldMatchIncompletePattern1() {
        GenericPattern<Integer> matchingPattern = GenericPattern
                .<Integer>instance()
                .beginning()
                .token(2)
                .token(15)
                .token(16).zeroOrOne()
                .token(17).zeroOrOne()
                .token(18).zeroOrOne()
                .build();
        Matcher<Integer> matcher = matchingPattern.matcher(SEQUENCE);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(0, matcher.getStart());
        Assert.assertEquals(2, matcher.getSize());
    }

    @Test
    public void shouldMatchIncompletePattern2() {
        GenericPattern<Integer> matchingPattern = GenericPattern
                .<Integer>instance()
                .token(42)
                .token(42)
                .token(15)
                .token(16).zeroOrOne()
                .token(17).zeroOrOne()
                .token(18).zeroOrOne()
                .build();
        Matcher<Integer> matcher = matchingPattern.matcher(SEQUENCE);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(2, matcher.getStart());
        Assert.assertEquals(3, matcher.getSize());
    }

    @Test
    public void shouldProcessMatchAndGetHits() {
        String testCase = "abcabacabccbaeacbabc";
        Character[] testCaseArray = ArrayUtils.toObject(testCase.toCharArray());

        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("c\\w+e", testCase);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(2, matcher.getStart());

        Group group = matcher.getGroup();
        Assert.assertNotNull(group);

        List<Character> hits = group.getHits(testCaseArray);
        Assert.assertNotNull(hits);
        Assert.assertEquals(
                "cabacabccbae",
                hits.stream().map(Object::toString).collect(Collectors.joining()));
    }

    @Test
    public void shouldProcessMatchWithGroups() {
        String testCase = "abcabacabccbaeacbabc";
        List<Character> testCaseCollection = testCase.chars().mapToObj(c -> (char) c).collect(Collectors.toList());

        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("c(\\w+)e", testCase);
        Assert.assertTrue(matcher.find());

        Group group = matcher.getGroup(1);
        Assert.assertNotNull(group);

        Character[] hits = group.getHitArray(testCaseCollection);
        Assert.assertNotNull(hits);
        Assert.assertEquals(
                "abacabccba",
                Arrays.stream(hits).map(Object::toString).collect(Collectors.joining()));
    }

    @Test
    public void shouldProcessMatchWithNestedGroups() {
        String testCase = "abcabacabccbaeacbabc";
        List<Character> testCaseCollection = testCase.chars().mapToObj(c -> (char) c).collect(Collectors.toList());

        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("c+(\\w+(cb*(a?)))e", testCase);
        Assert.assertTrue(matcher.find());

        Group group = matcher.getGroup(1);
        Assert.assertNotNull(group);

        Character[] hits = group.getHitArray(testCaseCollection);
        Assert.assertNotNull(hits);
        Assert.assertEquals(
                "abacabccba",
                Arrays.stream(hits).map(Object::toString).collect(Collectors.joining()));

        Group nestedGroup = matcher.getGroup(2);
        Assert.assertNotNull(nestedGroup);

        hits = nestedGroup.getHitArray(testCaseCollection);
        Assert.assertNotNull(hits);
        Assert.assertEquals(
                "cba",
                Arrays.stream(hits).map(Object::toString).collect(Collectors.joining()));

        Group nestedNestedGroup = matcher.getGroup(3);
        Assert.assertNotNull(nestedNestedGroup);
        hits = nestedNestedGroup.getHitArray(testCaseCollection);
        Assert.assertNotNull(hits);
        Assert.assertEquals(
                "a",
                Arrays.stream(hits).map(Object::toString).collect(Collectors.joining()));
    }

    @Test
    public void shouldProcessSerialMatches1() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("ab.", "abcabacabccbaacbabc");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(0, matcher.getStart());
        matcher.find();
        Assert.assertEquals(3, matcher.getStart());
        matcher.find();
        Assert.assertEquals(7, matcher.getStart());
        matcher.find();
        Assert.assertEquals(16, matcher.getStart());
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void shouldProcessSerialMatches2() {
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(num -> num % 10 == 2)
                .matcher(SEQUENCE);

        List<Integer> hits = new ArrayList<>();
        while (matcher.find()) {
            Group group = matcher.getGroup(0);
            Assert.assertNotNull(group);
            List<Integer> moreHits = group.getHits(SEQUENCE);
            Assert.assertNotNull(moreHits);
            int hit = moreHits.get(0);
            hits.add(hit);
        }
        Assert.assertEquals(3, hits.size());
        Assert.assertEquals(2, (int) hits.get(0));
        Assert.assertEquals(42, (int) hits.get(1));
        Assert.assertEquals(42, (int) hits.get(2));
    }

    @Test
    public void shouldProcessAlternatives() {
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(2)
                .token(16).or(15)
                .token(42)
                .matcher(SEQUENCE);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(0, matcher.getStart());
        Assert.assertEquals(3, matcher.getSize());

        matcher = GenericPattern
                .<Integer>instance()
                .token(2)
                .token(GenericPattern.<Integer>instance().token(16))
                    .or(GenericPattern.<Integer>instance().token(15).token(42).zeroOrMore())
                .matcher(SEQUENCE);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(0, matcher.getStart());
        Assert.assertEquals(4, matcher.getSize());

        matcher = GenericPattern
                .<Integer>instance()
                .token(16)
                    .or(15)
                    .or(t -> t % 10 == 2).tag("group").oneOrMore()
                .matcher(SEQUENCE);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(0, matcher.getStart());
        Assert.assertEquals(5, matcher.getSize());
    }
}

