package com.navercorp.pinpoint.profiler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringMatchUtilsTest {


    @Test
    public void indexOf() {
        Assertions.assertEquals(0, StringMatchUtils.indexOf("abc", "a".toCharArray()));
        Assertions.assertEquals(1, StringMatchUtils.indexOf("abc", "b".toCharArray()));
        Assertions.assertEquals(2, StringMatchUtils.indexOf("abc", "c".toCharArray()));
        Assertions.assertEquals(-1, StringMatchUtils.indexOf("abc", "d".toCharArray()));
    }

    @Test
    public void contains() {
        Assertions.assertTrue(StringMatchUtils.contains('a', "abc".toCharArray()));
        Assertions.assertTrue(StringMatchUtils.contains('c', "c".toCharArray()));
        Assertions.assertFalse(StringMatchUtils.contains('a', "bcd".toCharArray()));
    }

    @Test
    public void lastCountMatches1() {
        Assertions.assertEquals(0, StringMatchUtils.endsWithCountMatches("abc", "[]"));
        Assertions.assertEquals(1, StringMatchUtils.endsWithCountMatches("abc[]", "[]"));
        Assertions.assertEquals(2, StringMatchUtils.endsWithCountMatches("abc[][]", "[]"));
        Assertions.assertEquals(3, StringMatchUtils.endsWithCountMatches("[][][]", "[]"));
    }

    @Test
    public void lastCountMatches2() {
        Assertions.assertEquals(0, StringMatchUtils.endsWithCountMatches("[][]]", "[]"));
        Assertions.assertEquals(0, StringMatchUtils.endsWithCountMatches("[]]]", "[]"));
    }

    @Test
    public void lastCountMatches_invalid() {
        Assertions.assertEquals(1, StringMatchUtils.endsWithCountMatches("[][]abc[]", "[]"));
        Assertions.assertEquals(0, StringMatchUtils.endsWithCountMatches("[][]abc", "[]"));
        Assertions.assertEquals(0, StringMatchUtils.endsWithCountMatches("[]abc]", "[]"));

        Assertions.assertEquals(0, StringMatchUtils.endsWithCountMatches("a", "[][]"));

    }

    @Test
    public void startCountMatches1() {
        Assertions.assertEquals(0, StringMatchUtils.startsWithCountMatches("abc", '['));
        Assertions.assertEquals(1, StringMatchUtils.startsWithCountMatches("[abc", '['));
        Assertions.assertEquals(2, StringMatchUtils.startsWithCountMatches("[[abc", '['));
        Assertions.assertEquals(3, StringMatchUtils.startsWithCountMatches("[[[abc", '['));
    }

    @Test
    public void appendAndReplace() {
        StringBuilder buffer = new StringBuilder();
        StringMatchUtils.appendAndReplace("a.b.c", 0, '.', '/', buffer);

        Assertions.assertEquals("a/b/c", buffer.toString());
    }

    @Test
    public void appendAndReplace_offset() {
        StringBuilder buffer = new StringBuilder();
        StringMatchUtils.appendAndReplace("a.b.c", 2, '.', '/', buffer);

        Assertions.assertEquals("b/c", buffer.toString());
    }
}