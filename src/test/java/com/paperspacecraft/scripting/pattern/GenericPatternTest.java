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
                .token("a").tag("a")
                .token("b").tag("b")
                .group(GenericPattern.instance().token("d").build())
                .token("c").tag("c")
                .oneOrMore()
                .build();
    }

    @Test
    public void shouldCreateTaggedPattern() {
        // The "overall" always represents a capturing group
        Assert.assertTrue(pattern instanceof GroupMatching);
        GenericPattern<Object> parent = ((GroupMatching<Object>) pattern).getParent();
        Assert.assertEquals("a", parent.getTag());
        Assert.assertEquals("b", parent.getNext().getTag());
        Assert.assertEquals("c", parent.getNext().getNext().getNext().getTag());
    }

    @Test
    public void shouldCreateUpstreamLinks() {
        Assert.assertTrue(pattern instanceof GroupMatching);
        GenericPattern<Object> parent = ((GroupMatching<Object>) pattern).getParent();
        Assert.assertTrue(parent.getNext().getNext() instanceof GroupMatching);
        Assert.assertEquals(
                ((GroupMatching<Object>) parent.getNext().getNext()).getParent().getUpstream(),
                parent.getNext().getNext().getNext());
    }
}
