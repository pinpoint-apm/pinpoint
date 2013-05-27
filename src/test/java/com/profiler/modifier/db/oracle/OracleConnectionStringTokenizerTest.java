package com.profiler.modifier.db.oracle;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class OracleConnectionStringTokenizerTest {

    @Test
    public void trimLeft() {
        assertTrimLeft("123", "123");
        assertTrimLeft(" 123", "123");
        assertTrimLeft("  123 ", "123 ");

        assertTrimLeft("  1 23 ", "1 23 ");

        assertTrimLeft("  1 23 ", "1 23 ");

        assertTrimLeft("", "");
        assertTrimLeft("  ", "");

        assertTrimLeft("12 ", "12 ");

        assertTrimLeft("12 ", "12 ");
    }



    private void assertTrimLeft(String token, String result) {
        OracleConnectionStringTokenizer oracleConnectionStringTokenizer = new OracleConnectionStringTokenizer(token);
        int leftTrimIndex = oracleConnectionStringTokenizer.trimLeft();
        Assert.assertEquals(wrap(result), wrap(token.substring(leftTrimIndex)));
    }



    @Test
    public void trimRight() {
        assertTrimRight("123", "123");
        assertTrimRight("123 ", "123");
        assertTrimRight("123    ", "123");

        assertTrimRight("1 23 ", "1 23");
        assertTrimRight("  1 23 ", "  1 23");

        assertTrimRight("", "");
        assertTrimRight("  ", "");
    }

    private void assertTrimRight(String token, String result) {
        OracleConnectionStringTokenizer oracleConnectionStringTokenizer = new OracleConnectionStringTokenizer(token);
        int rightTrimIndex = oracleConnectionStringTokenizer.trimRight(token.length());
        Assert.assertEquals(wrap(result), wrap(token.substring(0, rightTrimIndex)));
    }

    @Test
    public void parseLiteral() {
        AssertParseLiteral("abc", "abc");
        AssertParseLiteral(" abc", "abc");
        AssertParseLiteral("  abc", "abc");

        AssertParseLiteral("abc ", "abc");
        AssertParseLiteral("abc  ", "abc");

        AssertParseLiteral("a  c", "a  c");
        AssertParseLiteral(" a  c", "a  c");
        AssertParseLiteral("   a  c  ", "a  c");

    }

    @Test
    public void simpleParse() {
        assertCompareToken("a=b", "a", "=", "b");

        assertCompareToken("a = b", "a", "=", "b");

        assertCompareToken(" a = b ", "a", "=", "b");

    }

    private void assertCompareToken(String token, String... parsedTokens) {
        OracleConnectionStringTokenizer oracleConnectionStringTokenizer = new OracleConnectionStringTokenizer(token);
        oracleConnectionStringTokenizer.parse();

        int index = 0;
        while(true) {
            Token t = oracleConnectionStringTokenizer.nextToken();

            if (t == null) {
                break;
            }
            Assert.assertEquals(t.getToken(), parsedTokens[index++]);
        }
    }

    private void AssertParseLiteral(String token, String result) {
        OracleConnectionStringTokenizer oracleConnectionStringTokenizer = new OracleConnectionStringTokenizer(token);
        String literal = oracleConnectionStringTokenizer.parseLiteral();
        Assert.assertEquals(wrap(result), wrap(literal));
    }

    private String wrap(String str) {
        return "\"" + str + "\"";
    }
}
