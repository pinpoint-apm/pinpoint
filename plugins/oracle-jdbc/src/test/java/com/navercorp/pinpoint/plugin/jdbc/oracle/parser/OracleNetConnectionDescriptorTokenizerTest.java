/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.oracle.parser;


import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.plugin.jdbc.oracle.parser.OracleNetConnectionDescriptorTokenizer;
import com.navercorp.pinpoint.plugin.jdbc.oracle.parser.Token;

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
            // EOF makes test broken.
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
