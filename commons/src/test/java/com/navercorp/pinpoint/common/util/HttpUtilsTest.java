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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.Charsets;
import org.junit.Assert;

import org.junit.Test;


public class HttpUtilsTest {
    @Test
    public void contentTypeCharset1() {
        String test = "text/plain; charset=UTF-8";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals(Charsets.UTF_8.name(), charset);
    }

    @Test
    public void contentTypeCharset2() {
        String test = "text/plain; charset=UTF-8;";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals(Charsets.UTF_8.name(), charset);
    }

    @Test
    public void contentTypeCharset3() {
        String test = "text/plain; charset=UTF-8; test=a";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals(Charsets.UTF_8.name(), charset);
    }

    @Test
    public void contentTypeCharset4() {
        String test = "text/plain; charset= UTF-8 ; test=a";

        String charset = HttpUtils.parseContentTypeCharset(test);
        Assert.assertEquals(Charsets.UTF_8.name(), charset);
    }

}