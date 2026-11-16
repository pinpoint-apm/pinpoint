/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * The same request and expectations for every org.asynchttpclient line.
 */
final class AsyncHttpClientTester {

    private AsyncHttpClientTester() {
    }

    static void executeAndVerify(WebServer webServer) throws Exception {
        final String httpUrl = webServer.getCallHttpUrl();
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            final Response response = client.prepareGet(httpUrl).execute().get();
            Assertions.assertEquals(200, response.getStatusCode());
        }

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        final Method execute = DefaultAsyncHttpClient.class.getDeclaredMethod("execute", Request.class, AsyncHandler.class);
        verifier.verifyTrace(event("ASYNC_HTTP_CLIENT", execute, null, null, webServer.getHostAndPort(),
                annotation("http.url", httpUrl)));

        // the default handler of execute() runs on the event loop, one async invocation per callback
        final Method onStatusReceived = AsyncCompletionHandler.class.getMethod("onStatusReceived", HttpResponseStatus.class);
        verifier.verifyTrace(event("ASYNC", "Asynchronous Invocation"));
        verifier.verifyTrace(event("ASYNC_HTTP_CLIENT_INTERNAL", onStatusReceived, annotation("http.status.code", 200)));

        final Method onCompleted = AsyncCompletionHandler.class.getMethod("onCompleted");
        verifier.verifyTrace(event("ASYNC", "Asynchronous Invocation"));
        verifier.verifyTrace(event("ASYNC_HTTP_CLIENT_INTERNAL", onCompleted));

        verifier.verifyTraceCount(0);
    }
}
