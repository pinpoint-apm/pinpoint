/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.httpclient4;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EndPointUtilsTest {


//    @Test
    public void getHostAndPort() throws Exception {
        // TODO Support final class mocking
        HttpRoute httpRoute = mock(HttpRoute.class);
        HttpHost httpHost = mock(HttpHost.class);
        when(httpHost.getHostName()).thenReturn("127.0.0.1");
        when(httpHost.getPort()).thenReturn(-1);
        when(httpRoute.getProxyHost()).thenReturn(httpHost);
        String hostAndPort = EndPointUtils.getHostAndPort(httpRoute);
    }

}