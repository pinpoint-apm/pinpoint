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

package com.navercorp.pinpoint.bootstrap.util;

import com.navercorp.pinpoint.common.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class AntPathMatcherTest {
    @Test
    public void isAntStyle() {
        Assertions.assertTrue(AntPathMatcher.isAntStylePattern("/*/test"));
        Assertions.assertTrue(AntPathMatcher.isAntStylePattern("/*/?"));
        Assertions.assertTrue(AntPathMatcher.isAntStylePattern("*.test"));
        Assertions.assertTrue(AntPathMatcher.isAntStylePattern("*.?"));

        Assertions.assertFalse(AntPathMatcher.isAntStylePattern("/abc/test"));
        Assertions.assertFalse(AntPathMatcher.isAntStylePattern("abc.test"));
    }

    @Test
    public void isMatched() {

        AntPathMatcher matcher = new AntPathMatcher("/test/?bc");
        Assertions.assertTrue(matcher.isMatched("/test/abc"));

        Assertions.assertFalse(matcher.isMatched("/test/axx"));


        Assertions.assertFalse(matcher.isMatched(null));
        Assertions.assertFalse(matcher.isMatched(""));
        Assertions.assertFalse(matcher.isMatched("test"));
    }

    @Test
    public void asteriskTest() {
        String version = Version.VERSION;

        String linuxPath = "/home/.m2/repository/pinpoint-mssql-jdbc-driver-plugin/" + version + "/pinpoint-mssql-jdbc-driver-plugin-" + version + ".jar";
        String windowsPath = "C:\\.m2\\repository\\pinpoint-mssql-jdbc-driver-plugin\\" + version + "\\pinpoint-mssql-jdbc-driver-plugin-" + version + ".jar";

        AntPathMatcher linuxMatcher = new AntPathMatcher("/**/pinpoint-*-plugin-" + version + ".jar");
        Assertions.assertTrue(linuxMatcher.isMatched(linuxPath));
        Assertions.assertFalse(linuxMatcher.isMatched(windowsPath));

        AntPathMatcher windowsMatcher = new AntPathMatcher("**\\pinpoint-*-plugin-" + version + ".jar");
        Assertions.assertFalse(windowsMatcher.isMatched(linuxPath));
        Assertions.assertTrue(windowsMatcher.isMatched(windowsPath));
    }


    @Test
    public void isMatchedDotSeparator() {
        final String pathSeparator = ".";
        AntPathMatcher matcher = new AntPathMatcher("test.?bc", pathSeparator);
        Assertions.assertTrue(matcher.isMatched("test.abc"));

        Assertions.assertFalse(matcher.isMatched("test.axx"));


        Assertions.assertFalse(matcher.isMatched(null));
        Assertions.assertFalse(matcher.isMatched(""));
        Assertions.assertFalse(matcher.isMatched("test"));
    }

}