package com.paperspacecraft.scripting.pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GenericPatternTest.class,
        NumericPatternsTest.class,
        PseudoRegexPatternsTest.class,
        PseudoRegexGroupsTest.class
})
public class AllTests {
}
