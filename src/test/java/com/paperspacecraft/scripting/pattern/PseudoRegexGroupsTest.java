package com.paperspacecraft.scripting.pattern;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PseudoRegexGroupsTest {

    @Test
    public void shouldCaptureGroup() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(bc)d", "abcd");
        Assert.assertTrue(matcher.find());

        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());
    }

    @Test
    public void shouldCaptureNestedGroups() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(b(cd))e", "abcdef");
        Assert.assertTrue(matcher.find());

        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(3, matcher.getGroups().size());

        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(3, matcher.getGroups().get(1).getSize());

        Assert.assertEquals(2, matcher.getGroups().get(2).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(2).getSize());
    }

    @Test
    public void shouldCaptureNestedAndSiblingGroups() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(b(cd))(e)", "abcdef");
        Assert.assertTrue(matcher.find());

        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(4, matcher.getGroups().size());

        Assert.assertEquals(4, matcher.getGroups().get(3).getStart());
        Assert.assertEquals(1, matcher.getGroups().get(3).getSize());
    }

    @Test
    public void shouldCaptureGroupWithPositioning() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("ab(c(d))$", "abcd");
        Assert.assertTrue(matcher.find());

        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(3, matcher.getGroups().size());
        Assert.assertEquals(2, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("^((a)bc)+", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(3, matcher.getGroups().size());
        Assert.assertEquals(0, matcher.getGroups().get(1).getStart());

        matcher = PseudoRegexTestsHelper.getMatcher("^(bcd)e", "abcde");
        Assert.assertFalse(matcher.find());
        matcher = PseudoRegexTestsHelper.getMatcher("ab(cd)$", "abcde");
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void shouldCaptureMultipleGroups() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(bc)(de)f", "abcdefg");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(3, matcher.getGroups().size());
        Assert.assertEquals(matcher.getStart(), matcher.getGroups().get(0).getStart());
        Assert.assertEquals(matcher.getSize(), matcher.getGroups().get(0).getSize());

        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());

        Assert.assertEquals(3, matcher.getGroups().get(2).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(2).getSize());
    }

    @Test
    public void shouldCaptureZeroOrMoreQuantifiedGroup() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(bc)*d", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());

        matcher = PseudoRegexTestsHelper.getMatcher("(abc)*d", "abcabcde");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(3, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(3, matcher.getGroups().get(1).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("((abc)*)d", "abcabcde");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(3, matcher.getGroups().size());
        Assert.assertEquals(0, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(6, matcher.getGroups().get(1).getSize());
        Assert.assertEquals(3, matcher.getGroups().get(2).getStart());
        Assert.assertEquals(3, matcher.getGroups().get(2).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)*", "abc");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)*d", "ad");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(1, matcher.getGroups().size());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)*d", "abd");
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void shouldCaptureOneOrMoreQuantifiedGroup() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(bc)+d", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());

        matcher = PseudoRegexTestsHelper.getMatcher("((abc)+)d", "abcabcde");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(3, matcher.getGroups().size());
        Assert.assertEquals(0, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(6, matcher.getGroups().get(1).getSize());
        Assert.assertEquals(3, matcher.getGroups().get(2).getStart());
        Assert.assertEquals(3, matcher.getGroups().get(2).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)+d", "ad");
        Assert.assertFalse(matcher.find());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)+d", "abd");
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void shouldCaptureZeroOrOneQuantifiedGroup() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(bc)?d", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());

        matcher = PseudoRegexTestsHelper.getMatcher("b(cd)?", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)?d", "ad");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(1, matcher.getGroups().size());

        matcher = PseudoRegexTestsHelper.getMatcher("a(bc)?d", "abd");
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void shouldCapturePreciseQuantifiedGroup() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("(ab){1,2}c", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(0, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("(bc){0,2}", "abc");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());
    }

    @Test
    public void shouldCaptureGroupWithInsideQuantifier() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(be?)+cd", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(1, matcher.getGroups().get(1).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("a(\\w+)d", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());

        matcher = PseudoRegexTestsHelper.getMatcher("(cd\\w*)", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(2, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());
    }

    @Test
    public void shouldCaptureAlternativeGroups() {
        Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher("a(de|bc)d", "abcd");
        Assert.assertTrue(matcher.find());
        Assert.assertNotNull(matcher.getGroups());
        Assert.assertEquals(2, matcher.getGroups().size());
        Assert.assertEquals(1, matcher.getGroups().get(1).getStart());
        Assert.assertEquals(2, matcher.getGroups().get(1).getSize());
        Assert.assertFalse(matcher.find());
    }

    @Test
    public void shouldCaptureAlternativeGroupsWithQuantifier() {
        List<String> patterns = Arrays.asList(
                "\\w(bar|der)+",
                "\\w(bar|der)*",
                "\\w(bar|der)?");
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            Matcher<Character> matcher = PseudoRegexTestsHelper.getMatcher(pattern, "debarbarcadere");
            Assert.assertTrue(matcher.find());
            Assert.assertNotNull(matcher.getGroup());
            Assert.assertNotNull(matcher.getGroups());
            Assert.assertEquals(2, matcher.getGroups().size());
            Assert.assertEquals(1, matcher.getGroup().getStart());
            Assert.assertEquals(i < 2 ? 7 : 4, matcher.getGroup().getSize());

            Assert.assertTrue(matcher.find());
            Assert.assertNotNull(matcher.getGroup());
            Assert.assertEquals(9, matcher.getGroup().getStart());
            Assert.assertEquals(4, matcher.getGroup().getSize());
            Assert.assertNotNull(matcher.getGroups());
            Group capturingGroup = matcher.getGroups().get(1);
            Assert.assertEquals(10, capturingGroup.getStart());
            Assert.assertEquals(3, capturingGroup.getSize());
        }
    }
}