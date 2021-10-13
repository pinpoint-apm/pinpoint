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

public class MavenVersionTest {
    protected static final int X_LT_Y = -1;

    protected static final int X_EQ_Y = 0;

    protected static final int X_GT_Y = 1;

    @Test
    public void compareTo() {
        assertEquals(X_LT_Y, compare(newVersion("1"), (newVersion("10"))));
        assertEquals(X_LT_Y, compare(newVersion("1.1"), (newVersion("1.10"))));
        assertEquals(X_LT_Y, compare(newVersion("1.0.1"), (newVersion("1.0.10"))));
        assertEquals(X_LT_Y, compare(newVersion("1.3658"), (newVersion("2"))));

        assertEquals(X_EQ_Y, compare(newVersion("1.0"), (newVersion("1-0"))));
        assertEquals(X_EQ_Y, compare(newVersion("1.0"), (newVersion("1_0"))));
        assertEquals(X_EQ_Y, compare(newVersion("1.2"), (newVersion("1.002"))));
        assertEquals(X_EQ_Y, compare(newVersion("1.0-ga"), (newVersion("1.0.0-ga"))));
        assertEquals(X_EQ_Y, compare(newVersion("1.0-alpha"), (newVersion("1.0.0-alpha"))));
        assertEquals(X_EQ_Y, compare(newVersion("1"), (newVersion("1............."))));
        assertEquals(X_EQ_Y, compare(newVersion("0.1"), (newVersion(".1"))));
        assertEquals(X_EQ_Y, compare(newVersion("1.0.1"), (newVersion("1..1"))));
        assertEquals(X_EQ_Y, compare(newVersion("1alpha10"), (newVersion("1.alpha.10"))));

        assertEquals(X_GT_Y, compare(newVersion("1-abc"), (newVersion("1-alpha"))));
        assertEquals(X_GT_Y, compare(newVersion("1.0a"), (newVersion("1.0"))));
        assertEquals(X_GT_Y, compare(newVersion("1.0a.1"), (newVersion("1.0"))));
        assertEquals(X_GT_Y, compare(newVersion("1.0m-1"), (newVersion("1.0"))));
        assertEquals(X_GT_Y, compare(newVersion("1-abc"), (newVersion("1-aac"))));
    }

    private MavenVersion newVersion(String version) {
        return new MavenVersion(version);
    }

    private int compare(MavenVersion arg1, MavenVersion arg2) {
        return arg1.compareTo(arg2);
    }
}