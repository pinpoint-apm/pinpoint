/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.util.apache;

import org.junit.Test;

import static org.junit.Assert.*;

public class MavenVersionRangeTest {

    @Test
    public void containsVersion() {
        assertTrue(newRange("[1,2]").containsVersion(newVersion("1")));
        assertTrue(newRange("[1,2]").containsVersion(newVersion("1.1-SNAPSHOT")));
        assertTrue(newRange("[1,2]").containsVersion(newVersion("2")));


        assertTrue(newRange("[1.2.3.4.5,1.2.3.4.6)").containsVersion(newVersion("1.2.3.4.5")));
        assertFalse(newRange("[1.2.3.4.5,1.2.3.4.6)").containsVersion(newVersion("1.2.3.4.6")));

        assertFalse(newRange("(1a,1b]").containsVersion(newVersion("1a")));
        assertTrue(newRange("(1a,1b]").containsVersion(newVersion("1b")));

        assertTrue(newRange("[1.2.*]").containsVersion(newVersion("1.2-alpha-1")));
        assertTrue(newRange("[1.2.*]").containsVersion(newVersion("1.2-SNAPSHOT")));
        assertFalse(newRange("[1.2.*]").containsVersion(newVersion("1.3-rc-1")));
    }

    private MavenVersionRange newRange(String versionRange) {
        return new MavenVersionRange(versionRange);
    }

    private MavenVersion newVersion(String version) {
        return new MavenVersion(version);
    }
}