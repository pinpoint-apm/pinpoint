package com.navercorp.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;

public class StringMatchUtilsTest {


    @Test
    public void indexOf() {
        Assert.assertEquals(0, StringMatchUtils.indexOf("abc", "a".toCharArray()));
        Assert.assertEquals(1, StringMatchUtils.indexOf("abc", "b".toCharArray()));
        Assert.assertEquals(2, StringMatchUtils.indexOf("abc", "c".toCharArray()));
        Assert.assertEquals(-1, StringMatchUtils.indexOf("abc", "d".toCharArray()));
    }

    @Test
    public void contains() {
        Assert.assertTrue(StringMatchUtils.contains('a', "abc".toCharArray()));
        Assert.assertTrue(StringMatchUtils.contains('c', "c".toCharArray()));
        Assert.assertFalse(StringMatchUtils.contains('a', "bcd".toCharArray()));
    }

    @Test
    public void lastCountMatches1() {
        Assert.assertEquals(0, StringMatchUtils.endsWithCountMatches("abc", "[]"));
        Assert.assertEquals(1, StringMatchUtils.endsWithCountMatches("abc[]", "[]"));
        Assert.assertEquals(2, StringMatchUtils.endsWithCountMatches("abc[][]", "[]"));
        Assert.assertEquals(3, StringMatchUtils.endsWithCountMatches("[][][]", "[]"));
    }

    @Test
    public void lastCountMatches2() {
        Assert.assertEquals(0, StringMatchUtils.endsWithCountMatches("[][]]", "[]"));
        Assert.assertEquals(0, StringMatchUtils.endsWithCountMatches("[]]]", "[]"));
    }

    @Test
    public void lastCountMatches_invalid() {
        Assert.assertEquals(1, StringMatchUtils.endsWithCountMatches("[][]abc[]", "[]"));
        Assert.assertEquals(0, StringMatchUtils.endsWithCountMatches("[][]abc", "[]"));
        Assert.assertEquals(0, StringMatchUtils.endsWithCountMatches("[]abc]", "[]"));

        Assert.assertEquals(0, StringMatchUtils.endsWithCountMatches("a", "[][]"));

    }

    @Test
    public void startCountMatches1() {
        Assert.assertEquals(0, StringMatchUtils.startsWithCountMatches("abc", '['));
        Assert.assertEquals(1, StringMatchUtils.startsWithCountMatches("[abc", '['));
        Assert.assertEquals(2, StringMatchUtils.startsWithCountMatches("[[abc", '['));
        Assert.assertEquals(3, StringMatchUtils.startsWithCountMatches("[[[abc", '['));
    }

    @Test
    public void appendAndReplace() {
        StringBuilder buffer = new StringBuilder();
        StringMatchUtils.appendAndReplace("a.b.c", 0, '.', '/', buffer);

        Assert.assertEquals("a/b/c", buffer.toString());
    }

    @Test
    public void appendAndReplace_offset() {
        StringBuilder buffer = new StringBuilder();
        StringMatchUtils.appendAndReplace("a.b.c", 2, '.', '/', buffer);

        Assert.assertEquals("b/c", buffer.toString());
    }
}