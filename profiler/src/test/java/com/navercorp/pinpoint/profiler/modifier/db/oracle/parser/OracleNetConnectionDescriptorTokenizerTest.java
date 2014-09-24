package com.nhn.pinpoint.profiler.modifier.db.oracle.parser;


import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class OracleNetConnectionDescriptorTokenizerTest {

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
        OracleNetConnectionDescriptorTokenizer tokenizer = new OracleNetConnectionDescriptorTokenizer(token);
        int leftTrimIndex = tokenizer.trimLeft();
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
        OracleNetConnectionDescriptorTokenizer tokenizer = new OracleNetConnectionDescriptorTokenizer(token);
        int rightTrimIndex = tokenizer.trimRight(token.length());
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
        OracleNetConnectionDescriptorTokenizer tokenizer = new OracleNetConnectionDescriptorTokenizer(token);
        tokenizer.parse();

        int index = 0;
        while(true) {
            Token t = tokenizer.nextToken();
            // EOF 오브젝트가 추가되서 테스트가 깨짐.
            if (t != null && t == OracleNetConnectionDescriptorTokenizer.TOKEN_EOF_OBJECT) {
                return;
            }

            if (t == null) {
                break;
            }
            Assert.assertEquals(t.getToken(), parsedTokens[index++]);
        }
    }

    private void AssertParseLiteral(String token, String result) {
        OracleNetConnectionDescriptorTokenizer tokenizer = new OracleNetConnectionDescriptorTokenizer(token);
        String literal = tokenizer.parseLiteral();
        Assert.assertEquals(wrap(result), wrap(literal));
    }

    private String wrap(String str) {
        return "\"" + str + "\"";
    }
}
