package com.paperspacecraft.scripting.pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

public class PseudoRegexPatternsTest {

    @Test
    public void shouldFindSimpleSequences() {
        PseudoRegexTestsHelper.assertMatch("abcd", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("bcd", "abcd", 1, 3);

        PseudoRegexTestsHelper.assertNotMatch("abcd", "abce");
        PseudoRegexTestsHelper.assertNotMatch("abcd", "abce");
        PseudoRegexTestsHelper.assertNotMatch("abcd$", "abcde");
    }

    @Test
    public void shouldFindSequencesWithPositioning() {
        PseudoRegexTestsHelper.assertMatch("^abcd$", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("bc", "abcd", 1, 2);
        PseudoRegexTestsHelper.assertMatch("bcd$", "abcd", 1, 3);
        PseudoRegexTestsHelper.assertMatch("^abc", "abcd", 0, 3);

        PseudoRegexTestsHelper.assertNotMatch("abc$", "abcd");
        PseudoRegexTestsHelper.assertNotMatch("^bcd", "abcd");
    }

    @Test
    public void shouldFindByWildcards() {
        PseudoRegexTestsHelper.assertMatch("abc.e", "abcde", 0, 5);
        PseudoRegexTestsHelper.assertMatch(".....", "abcfe", 0, 5);
        PseudoRegexTestsHelper.assertMatch("...", "abcfe", 0, 3);
        PseudoRegexTestsHelper.assertMatch("\\w\\w\\d\\d", "abc42e", 1, 4);

        PseudoRegexTestsHelper.assertNotMatch("abc.e", "abcdf");
        PseudoRegexTestsHelper.assertNotMatch("......", "abcde");
    }

    @Test
    public void shouldProcessZeroOrMoreQuantifier() {
        PseudoRegexTestsHelper.assertMatch(".*", "abcde", 0, 5);
        PseudoRegexTestsHelper.assertMatch("a.*", "abcde", 0, 5);
        PseudoRegexTestsHelper.assertMatch("ab.*e", "abcde", 0, 5);
        PseudoRegexTestsHelper.assertMatch("abc*e", "abe", 0, 3);
        PseudoRegexTestsHelper.assertMatch("e.*", "abe", 2, 1);

        PseudoRegexTestsHelper.assertNotMatch("f.*", "abcde");
        PseudoRegexTestsHelper.assertNotMatch("a.*f", "abcde");
    }

    @Test
    public void shouldProcessOneOrMoreQuantifier() {
        PseudoRegexTestsHelper.assertMatch("abc+d", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("abc+d", "abccd", 0, 5);
        PseudoRegexTestsHelper.assertMatch("bc+d$", "abcccd", 1, 5);

        PseudoRegexTestsHelper.assertNotMatch("abc+d", "abd");
        PseudoRegexTestsHelper.assertNotMatch("abcd+", "abce");
        PseudoRegexTestsHelper.assertNotMatch("^bc+d", "abcccd");
    }

    @Test
    public void shouldProcessZeroOrOneQuantifier() {
        PseudoRegexTestsHelper.assertMatch("abc?d", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("abe?", "abd", 0, 2);

        PseudoRegexTestsHelper.assertNotMatch("abc?d", "abe");
    }

    @Test
    public void shouldProcessNumericQuantifier() {
        PseudoRegexTestsHelper.assertMatch("abc{1,2}d", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("abcd{1,2}", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("\\w{1,4}", "abcd", 0, 4);

        PseudoRegexTestsHelper.assertNotMatch("abc{1,3}d", "abd");
        PseudoRegexTestsHelper.assertNotMatch("abc{2,3}d", "abcd");
    }

    @Test
    public void shouldHandleGreedySearch() {
        PseudoRegexTestsHelper.assertMatch("ab*c*", "abcd", 0, 3);
        PseudoRegexTestsHelper.assertMatch("ab?c?", "abcd", 0, 3);
        PseudoRegexTestsHelper.assertMatch("ab+c*.", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch(".*", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch(".*d", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch(".+.*d", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch(".{2}.$", "abcd", 1, 3);
    }

    @Test
    public void shouldCaptureAlternatives1() {
        PseudoRegexTestsHelper.assertMatch("a[bc]d", "abd", 0, 3);
        PseudoRegexTestsHelper.assertMatch("a[bc]d", "acd", 0, 3);
        PseudoRegexTestsHelper.assertMatch("^[dae]\\w", "abcd", 0, 2);
        PseudoRegexTestsHelper.assertMatch("[dc]$", "abcd", 3, 1);
        PseudoRegexTestsHelper.assertMatch("^[dcba]+", "abcd", 0, 4);
        PseudoRegexTestsHelper.assertMatch("[abc]*", "abacabda", 0, 6);
    }

    @Test
    public void shouldCaptureAlternatives2() {
        Matcher<Character> matcher = GenericPattern.<Character>instance()
                .any()
                .token('a').or('b').or('c').or('d').or('e').oneOrMore()
                .matcher(ArrayUtils.toObject("debarcadere".toCharArray()));

        Assert.assertTrue(matcher.find());
        Assert.assertEquals(0, matcher.getStart()); // "deba"
        Assert.assertEquals(4, matcher.getSize());
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(4, matcher.getStart()); // "rcade"
        Assert.assertEquals(5, matcher.getSize());
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(9, matcher.getStart()); // "re"
        Assert.assertEquals(2, matcher.getSize());
        Assert.assertFalse(matcher.find());
    }
}
