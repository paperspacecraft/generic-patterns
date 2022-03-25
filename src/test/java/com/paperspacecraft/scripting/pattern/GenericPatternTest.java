package com.paperspacecraft.scripting.pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenericPatternTest {

    GenericPattern<Object> pattern;

    @Before
    public void init() {
        pattern = GenericPattern
                .instance()
                .token("a1").or("a2").tag("a")
                .token("b").tag("b").oneOrMore()
                .token(GenericPattern.instance().token("g1.1").token("g1.2")).tag("g1")
                .token("c1")
                    .or("c2")
                    .or("с3")
                    .tag("c")
                .token(GenericPattern.instance().token("g2.1").token("g2.2"))
                    .or(GenericPattern.instance().token("g2.3").token("g2.4"))
                    .tag("g2")
                .token("d").tag("d")
                .build();
    }

    @Test
    public void shouldCreateTaggedPattern() {
        // The "overall" always represents a capturing group
        Assert.assertTrue(pattern instanceof GroupMatching);
        GenericPattern<Object> entryPoint = ((GroupMatching<Object>) pattern).getEntryPoint();
        Assert.assertEquals("a", entryPoint.getTag());
        Assert.assertEquals("b", entryPoint.getNext().getTag());
        Assert.assertEquals("g1", entryPoint.getNext().getNext().getTag());
        Assert.assertEquals("c", entryPoint.getNext().getNext().getNext().getTag());
    }

    @Test
    public void shouldCreateUpstreamLinks() {
        Assert.assertTrue(pattern instanceof GroupMatching);
        GenericPattern<Object> entryPoint = ((GroupMatching<Object>) pattern).getEntryPoint();
        Assert.assertTrue(entryPoint.getNext().getNext() instanceof GroupMatching);
        Assert.assertEquals(
                ((GroupMatching<Object>) entryPoint.getNext().getNext()).getEntryPoint().getNext().getUpstream(),
                entryPoint.getNext().getNext().getNext());
    }

    @Test
    public void shouldCreateAlternatives() {
        Assert.assertTrue(pattern instanceof GroupMatching);
        GenericPattern<Object> entryPoint = ((GroupMatching<Object>) pattern).getEntryPoint();
        Assert.assertTrue(entryPoint.getNext().getNext().getNext() instanceof AlternativeMatching);
        AlternativeMatching<Object> alternativeMatching = (AlternativeMatching<Object>) entryPoint.getNext().getNext().getNext();
        Assert.assertEquals(3, alternativeMatching.getAlternatives().size());
    }
}
