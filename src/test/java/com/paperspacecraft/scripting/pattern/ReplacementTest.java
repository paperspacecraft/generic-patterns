package com.paperspacecraft.scripting.pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ReplacementTest {

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
    public void shouldReplaceWithInflation() {
        PseudoRegexTestsHelper.assertReplacement(
                "abcabc",
                "\\w",
                "123",
                "123123123123123123");
    }

    @Test
    public void shouldReplaceWithModifier() {
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

    @Test
    public void shouldReplaceWithModifierAndInflation1() {
        List<Integer> numbers = Arrays.asList(5, 7, 15, 21);
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(i -> i % 10 == 5)
                .matcher(numbers);
        List<Integer> modifiedNumbers = matcher.replaceWithList(match -> {
            List<Integer> hits = match.getHits(numbers);
            Assert.assertNotNull(hits);
            return Arrays.asList(hits.get(0), hits.get(0));
        });
        Assert.assertTrue(CollectionUtils.isEqualCollection(modifiedNumbers, Arrays.asList(5, 5, 7, 15, 15, 21)));
    }

    @Test
    public void shouldReplaceWithModifierAndInflation2() {
        List<Integer> numbers = Arrays.asList(5, null, 7, 15, 21);
        Matcher<Integer> matcher = GenericPattern
                .<Integer>instance()
                .token(i -> i % 10 == 5)
                .matcher(numbers);
        List<Integer> modifiedNumbers = matcher.replaceWithList(match -> {
            List<Integer> hits = match.getHits(numbers);
            Assert.assertNotNull(hits);
            return Arrays.asList(hits.get(0), hits.get(0));
        });
        Assert.assertTrue(CollectionUtils.isEqualCollection(modifiedNumbers, Arrays.asList(5, 5, null, 7, 15, 15, 21)));
    }
}

