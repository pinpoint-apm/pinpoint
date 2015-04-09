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

package com.navercorp.pinpoint.plugin.httpclient4;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

/**
 * @author minwoo.jung 
 * @author jaehong.kim
 *
 */
public class HttpClient4PluginTest {

    @Test
    public void addDefaultHttpRequestRetryHandlerClass() {
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler();
        IOException iOException = new IOException();
        HttpContext context = new BasicHttpContext();

        assertTrue(retryHandler.retryRequest(iOException, 1, context));
        assertTrue(retryHandler.retryRequest(iOException, 2, context));
    }

}
