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

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

@RunWith(PinpointPluginTestSuite.class)
@Dependency({ "org.apache.httpcomponents:httpclient:[4.3],[4.3.1],[4.3.2],[4.3.3],[4.3.4],[4.4],[4.4.1],[4.5]" })
public class DefaultHttpRequestRetryHandlerModifierIT {

    @Test
    public void test() throws Exception {
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler();
        IOException iOException = new IOException();
        HttpContext context = new BasicHttpContext();
        
        assertTrue(retryHandler.retryRequest(iOException, 1, context));
        assertTrue(retryHandler.retryRequest(iOException, 2, context));
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", DefaultHttpRequestRetryHandler.class.getMethod("retryRequest", IOException.class, int.class, HttpContext.class),
                annotation("http.internal.display", IOException.class.getName() + ", 1"), annotation("RETURN_DATA", true)));
        
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", DefaultHttpRequestRetryHandler.class.getMethod("retryRequest", IOException.class, int.class, HttpContext.class),
                annotation("http.internal.display", IOException.class.getName() + ", 2"), annotation("RETURN_DATA", true)));

        verifier.verifyTraceCount(0);
    }
}
