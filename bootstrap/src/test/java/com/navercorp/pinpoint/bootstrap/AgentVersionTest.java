/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentVersionTest {

    private final Pattern versionPattern = Pattern.compile(AgentDirBaseClassPathResolver.VERSION_PATTERN);

    @Test
    public void testVersion() {

        assertVersion("-1.6.0");
        assertVersion("-2.1.0");
        assertVersion("-20.10.99");

        assertVersion("-1.6.0-SNAPSHOT");

        assertVersion("-1.6.0-RC1");
        assertVersion("-1.6.0-RC0");
        assertVersion("-1.6.0-RC11");


    }

    @Test
    public void testVersion_fail() {

        assertFalseVersion("-1.6.0-RC");
        assertFalseVersion("-2.1.0-SNAPSHOT-RC1");
    }

    private void assertVersion(String versionString) {
        Matcher matcher = this.versionPattern.matcher(versionString);
        Assert.assertTrue(matcher.matches());
    }

    private void assertFalseVersion(String versionString) {
        Matcher matcher = this.versionPattern.matcher(versionString);
        Assert.assertFalse(matcher.matches());
    }

}
