/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class CodeSourceUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testGetCodeLocationThenNPE() {
        CodeSourceUtils.getCodeLocation(null);
    }

    @Test
    public void testGetCodeLocationThenNull() {
        final URL url = CodeSourceUtils.getCodeLocation(Object.class);
        assertThat(url, nullValue());
    }

    @Test
    public void testGetCodeLocation() {
        final URL url = CodeSourceUtils.getCodeLocation(CodeSourceUtils.class);
        assertThat(url, notNullValue());
        assertThat(url.getPath(), endsWith("/pinpoint/test/target/classes/"));
    }
}