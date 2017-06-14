/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author jaehong.kim
 */
public class EndPointUtilsTest {

    @Test
    public void getEndPoint() throws Exception {
        assertEquals("127.0.0.1:80", EndPointUtils.getEndPoint("http://127.0.0.1:80", null));
        assertEquals("127.0.0.1:80", EndPointUtils.getEndPoint("http://127.0.0.1:80/path", null));
        assertEquals("127.0.0.1:80", EndPointUtils.getEndPoint("http://127.0.0.1:80?query=foo", null));
        assertEquals("127.0.0.1:80", EndPointUtils.getEndPoint("http://127.0.0.1:80/path?query=foo", null));
        assertEquals("127.0.0.1", EndPointUtils.getEndPoint("http://127.0.0.1", null));
        assertEquals("127.0.0.1", EndPointUtils.getEndPoint("http://127.0.0.1/path", null));
        assertEquals("127.0.0.1", EndPointUtils.getEndPoint("http://127.0.0.1?query=foo", null));
        assertEquals("127.0.0.1", EndPointUtils.getEndPoint("http://127.0.0.1/path?query=foo", null));

        assertEquals("127.0.0.1:443", EndPointUtils.getEndPoint("https://127.0.0.1:443", null));
        assertEquals("127.0.0.1", EndPointUtils.getEndPoint("https://127.0.0.1", null));

        assertEquals("127.0.0.1:99999", EndPointUtils.getEndPoint("http://127.0.0.1:99999", null));
        assertEquals("111111", EndPointUtils.getEndPoint("http://111111", null));

        assertEquals(null, EndPointUtils.getEndPoint(null, null));
        assertEquals(null, EndPointUtils.getEndPoint("", null));
        assertEquals(null, EndPointUtils.getEndPoint(" ", null));

        assertEquals("default", EndPointUtils.getEndPoint(null, "default"));
        assertEquals("default", EndPointUtils.getEndPoint("", "default"));
        assertEquals("default", EndPointUtils.getEndPoint(" ", "default"));

        assertEquals("127.0.0.1:number", EndPointUtils.getEndPoint("http://127.0.0.1:number", null));
        assertEquals("ftp:foo:bar@127.0.0.1", EndPointUtils.getEndPoint("ftp:foo:bar@127.0.0.1", null));
        assertEquals("mailto:foo@bar.com", EndPointUtils.getEndPoint("mailto:foo@bar.com", null));

    }
}