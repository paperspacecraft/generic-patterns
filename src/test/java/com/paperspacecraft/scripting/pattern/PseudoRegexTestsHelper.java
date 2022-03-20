package com.paperspacecraft.scripting.pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;

import java.util.regex.Pattern;

class PseudoRegexTestsHelper {

    private PseudoRegexTestsHelper() {
    }

    public static Matcher<Character> getMatcher(String regexPattern, String testCase) {
        GenericPattern<Character> genPattern = getPattern(regexPattern);
        Character[] chars = ArrayUtils.toObject(testCase.toCharArray());
        return genPattern.matcher(chars);
    }

    public static void assertMatch(String regexPattern, String testCase, int start, int size) {
        Assert.assertTrue(Pattern.compile(regexPattern).matcher(testCase).find());
        Matcher<Character> matcher = getMatcher(regexPattern, testCase);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(start, matcher.getStart());
        Assert.assertEquals(size, matcher.getSize());
    }

    public static void assertNotMatch(String regexPattern, String testCase) {
        Assert.assertFalse(Pattern.compile(regexPattern).matcher(testCase).find());
        Matcher<Character> matcher = getMatcher(regexPattern, testCase);
        Assert.assertFalse(matcher.find());
    }

    private static GenericPattern<Character> getPattern(String value) {
        return getPattern(value, 0, value.length());
    }

    private static GenericPattern<Character> getPattern(String value, int start, int end) {
        GenericPattern.Builder<Character> builder = GenericPattern.instance();

        int position = start;
        boolean isEscaping = false;

        while (position < end) {
            char current = value.charAt(position);
            if (current == '\\') {
                isEscaping = true;
                position++;
                continue;
            }
            if (isEscaping) {
                builder = processSpecialSequence(builder, "\\" + current);
                isEscaping = false;

            } else if (current == '^') {
                builder = ((GenericPattern.Starter<Character>) builder).beginning();

            } else if (current == '$') {
                builder = (GenericPattern.Builder<Character>) ((GenericPattern.Token<Character>) builder).ending();

            } else if (current == '.') {
                builder = builder.any().tag("*");

            } else if (isQuantifier(current)) {
                builder = processQuantifier((GenericPattern.Token<Character>) builder, current);

            } else if (current == '(') {
                Pair<GenericPattern.Builder<Character>, Integer> builderIntegerPair = processGroup(builder, value, position);
                builder = builderIntegerPair.getLeft();
                position = builderIntegerPair.getRight();

            } else if (current == '{') {
                Pair<Pair<Integer, Integer>, Integer> quantifierPair = processComplexQuantifier(value, position);
                Pair<Integer, Integer> quantifiers = quantifierPair.getLeft();
                builder = ((GenericPattern.Token<Character>) builder).count(quantifiers.getLeft(), quantifiers.getRight());
                position = quantifierPair.getRight();

            } else {
                builder = builder.token(current).tag(String.valueOf(current));
            }
            position++;
        }

        return builder.build();
    }

    private static GenericPattern.Builder<Character> processSpecialSequence(
            GenericPattern.Builder<Character> builder,
            String sequence) {

        if ("\\w".equals(sequence)) {
            return builder.token(Character::isAlphabetic);
        } else if ("\\d".equals(sequence)) {
            return builder.token(Character::isDigit);
        }
        return builder;
    }

    private static boolean isQuantifier(char value) {
        return value == '+' || value == '*' || value == '?';
    }

    private static GenericPattern.Builder<Character> processQuantifier(
            GenericPattern.Token<Character> builder,
            char quantifier) {

        if (quantifier == '+') {
            return builder.oneOrMore();
        } else if (quantifier == '*') {
            return builder.zeroOrMore();
        } else if (quantifier == '?') {
            return builder.zeroOrOne();
        }
        return builder;
    }

    private static Pair<Pair<Integer, Integer>, Integer> processComplexQuantifier(
            String value,
            int position) {

        int closingBracketPosition = value.indexOf('}', position);
        String numbers = value.substring(position + 1, closingBracketPosition);
        int lower;
        int upper;
        if (numbers.contains(",")) {
            lower = Integer.parseInt(StringUtils.substringBefore(numbers, ","));
            upper = Integer.parseInt(StringUtils.substringAfter(numbers, ","));
        } else {
            lower = Integer.parseInt(numbers);
            upper = lower;
        }
        return Pair.of(
                Pair.of(lower, upper),
                closingBracketPosition);
    }

    private static Pair<GenericPattern.Builder<Character>, Integer> processGroup(
            GenericPattern.Builder<Character> builder,
            String value,
            int position) {

        int closingBracketPosition = findClosingBracket(value, position);
        GenericPattern<Character> group = getPattern(value, position + 1, closingBracketPosition);
        return Pair.of(
                builder.group(group).tag('(' + value.substring(position + 1, closingBracketPosition) + ')'),
                closingBracketPosition);
    }

    private static int findClosingBracket(String value, int position) {
        int openingCount = 0;
        int closingCount = 0;
        for (int i = position; i < value.length(); i++) {
            if (value.charAt(i) == '(') {
                openingCount++;
            } else if (value.charAt(i) == ')') {
                closingCount++;
            }
            if (openingCount == closingCount) {
                return i;
            }
        }
        return -1;
    }
}
