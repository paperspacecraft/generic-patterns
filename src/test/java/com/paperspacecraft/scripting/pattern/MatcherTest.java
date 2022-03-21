package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatcherTest {

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
            Assert.assertNotNull(matcher.getGroups());
            int hit = matcher.getGroups().get(0).getHits(NumericPatternsTest.SEQUENCE).get(0);
            hits.add(hit);
        }
        Assert.assertEquals(3, hits.size());
        Assert.assertEquals(2, (int) hits.get(0));
        Assert.assertEquals(42, (int) hits.get(1));
        Assert.assertEquals(42, (int) hits.get(2));
    }

    @Test
    public void shouldReplaceSubsequences() {
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
    public void shouldReplaceSubsequencesViaModifier() {
        List<Integer> numbers = Arrays.asList(5, 5, 10, 17, 25, 7, 1, 45, 45, 2);
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(i -> i % 10 == 5).count(2)
                .matcher(numbers);
        List<Integer> modifiedNumbers = matcher.replaceAll(match -> {
            List<Integer> hits = match.getHits(numbers);
            Assert.assertNotNull(hits);
            return Arrays.asList(hits.get(0) * 10, hits.get(1) * 10);
        });
        Assert.assertTrue(CollectionUtils.isEqualCollection(modifiedNumbers, Arrays.asList(50, 50, 10, 17, 25, 7, 1, 450, 450, 2)));
    }
}
