/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.apache.httpcomponents:httpclient:[4.0],[4.0.1],[4.0.2],[4.0.3],[4.1],[4.1.1],[4.1.2],[4.1.3],[4.2],[4.2.1],[4.2.2],[4.2.3],[4.2.4],[4.2.4],[4.2.6],[4.3.3]"})
public class DefaultHttpRequestRetryHandlerModifierIT {

    @Test
    public void test() throws Exception {
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler();
        IOException iOException = new IOException();
        HttpContext context = new BasicHttpContext();
        
        Assertions.assertTrue(retryHandler.retryRequest(iOException, 1, context));
        Assertions.assertTrue(retryHandler.retryRequest(iOException, 2, context));
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", DefaultHttpRequestRetryHandler.class.getMethod("retryRequest", IOException.class, int.class, HttpContext.class),
                annotation("http.internal.display", IOException.class.getName() + ", 1"), annotation("RETURN_DATA", true)));
        
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", DefaultHttpRequestRetryHandler.class.getMethod("retryRequest", IOException.class, int.class, HttpContext.class),
                annotation("http.internal.display", IOException.class.getName() + ", 2"), annotation("RETURN_DATA", true)));

        verifier.verifyTraceCount(0);
    }
}
