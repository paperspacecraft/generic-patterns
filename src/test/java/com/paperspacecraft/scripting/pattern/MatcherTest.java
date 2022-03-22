package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MatcherTest {

    @Test
    public void shouldProcessMatchAndGetHits() {
        String testCase = "abcabacabccbaeacbabc";
        Character[] testCaseArray = ArrayUtils.toObject(testCase.toCharArray());

        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("c\\w+e", testCase);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(2, matcher.getStart());

        CapturingGroup group = matcher.getGroup();
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

        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("c(\\w+)e", "abcabacabccbaeacbabc");
        Assert.assertTrue(matcher.find());

        CapturingGroup group = matcher.getGroup(1);
        Assert.assertNotNull(group);

        Character[] hits = group.getHitArray(testCaseCollection);
        Assert.assertNotNull(hits);
        Assert.assertEquals(
                "abacabccba",
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
                .matcher(NumericPatternsTest.SEQUENCE);

        List<Integer> hits = new ArrayList<>();
        while (matcher.find()) {
            CapturingGroup group = matcher.getGroup(0);
            Assert.assertNotNull(group);
            List<Integer> moreHits = group.getHits(NumericPatternsTest.SEQUENCE);
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
    public void shouldReplace() {
        PseudoRegexTestsHelper.assertReplacement(
                "abcabacabccbaacbabc",
                "abc",
                "ABC",
                "ABCabacABCcbaacbABC");
        PseudoRegexTestsHelper.assertReplacement(
                "aaaabbbbccabcaabbcabbbbbbcdef",
                "a+b+c*",
                "abc",
                "abcabcabcabcdef");
        PseudoRegexTestsHelper.assertReplacement(
                "a123bc5461a87b456c",
                "\\d+",
                "",
                "abcabc");
    }

    @Test
    public void shouldReplaceWithModifier2() {
        List<Integer> numbers = Arrays.asList(5, 5, 10, 17, 25, 7, 1, 25, 25, 2);
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(i -> i % 10 == 5).count(2)
                .matcher(numbers);
        List<Integer> modifiedNumbers = matcher.replaceWith(match -> {
            List<Integer> hits = match.getHits(numbers);
            Assert.assertNotNull(hits);
            return hits.get(0) * hits.get(1);
        });
        Assert.assertTrue(CollectionUtils.isEqualCollection(modifiedNumbers, Arrays.asList(25, 10, 17, 25, 7, 1, 625, 2)));
    }
}
