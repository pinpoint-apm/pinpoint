/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.okhttp;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-okhttp-plugin")
@Dependency({"com.squareup.okhttp3:okhttp:[3.0.0,3.3.max]", WebServer.VERSION, PluginITConstants.VERSION})
public class OkHttpClient_3_0_0_to_3_3_x_IT extends OkHttpClient_3_BaseIT {
    static final String ASYNC = "ASYNC";
    static final String OK_HTTP_CLIENT = "OK_HTTP_CLIENT";
    static final String OK_HTTP_CLIENT_INTERNAL = "OK_HTTP_CLIENT_INTERNAL";

    private static WebServer webServer;


    @BeforeAll
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }


    @AfterAll
    public static void AfterClass() {
        webServer = WebServer.cleanup(webServer);
    }

    @Test
    public void execute() throws Exception {
        Request request = new Request.Builder().url(webServer.getCallHttpUrl()).build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method executeMethod = Class.forName("okhttp3.RealCall").getDeclaredMethod("execute");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, executeMethod));

        Method sendRequestMethod = Class.forName("okhttp3.internal.http.HttpEngine").getDeclaredMethod("sendRequest");
        verifier.verifyTrace(event(OK_HTTP_CLIENT, sendRequestMethod,
                null, null, webServer.getHostAndPort(),
                annotation("http.url", request.url().toString())));

        String hostAndPort = toHostAndPortString(request.url().host(), request.url().port());
        Method connectMethod = Class.forName("okhttp3.internal.http.HttpEngine").getDeclaredMethod("connect");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, connectMethod,
                annotation("http.internal.display", hostAndPort)));

        Method readResponseMethod = Class.forName("okhttp3.internal.http.HttpEngine").getDeclaredMethod("readResponse");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, readResponseMethod,
                annotation("http.status.code", response.code())
        ));

        verifier.verifyTraceCount(0);

        assertCaller(response);

    }

    @Test
    public void enqueue() throws Exception {
        Request request = new Request.Builder().url(webServer.getCallHttpUrl()).build();
        OkHttpClient client = new OkHttpClient();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Response> responseRef = new AtomicReference<>(null);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseRef.set(response);
                latch.countDown();
            }
        });
        latch.await(3, TimeUnit.SECONDS);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(event(ASYNC, "Asynchronous Invocation"), 20, 3000);
        verifier.printCache();

        Method realCallEnqueueMethod = Class.forName("okhttp3.RealCall").getDeclaredMethod("enqueue", Class.forName("okhttp3.Callback"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, realCallEnqueueMethod));

        Method dispatcherEnqueueMethod = Class.forName("okhttp3.Dispatcher").getDeclaredMethod("enqueue", Class.forName("okhttp3.RealCall$AsyncCall"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, dispatcherEnqueueMethod));

        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));

        Method executeMethod = Class.forName("okhttp3.RealCall$AsyncCall").getDeclaredMethod("execute");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, executeMethod));

        Method sendRequestMethod = Class.forName("okhttp3.internal.http.HttpEngine").getDeclaredMethod("sendRequest");
        verifier.verifyTrace(event(OK_HTTP_CLIENT, sendRequestMethod,
                null, null, webServer.getHostAndPort(),
                annotation("http.url", request.url().toString())));

        String hostAndPort = toHostAndPortString(request.url().host(), request.url().port());
        Method connectMethod = Class.forName("okhttp3.internal.http.HttpEngine").getDeclaredMethod("connect");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, connectMethod,
                annotation("http.internal.display", hostAndPort)));

        Response response = responseRef.get();
        Method readResponseMethod = Class.forName("okhttp3.internal.http.HttpEngine").getDeclaredMethod("readResponse");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, readResponseMethod,
                annotation("http.status.code", response.code()))
        );

        verifier.verifyTraceCount(0);
    }

    public static String toHostAndPortString(String host, int port) {
        return toHostAndPortString(host, port, -1);
    }

    /**
     * This API does not verification for input args.
     */
    public static String toHostAndPortString(String host, int port, int noPort) {
        // don't validation hostName
        // don't validation port range
        if (noPort == port) {
            return host;
        }
        final int hostLength = host == null ? 0 : host.length();
        final StringBuilder builder = new StringBuilder(hostLength + 6);
        builder.append(host);
        builder.append(':');
        builder.append(port);
        return builder.toString();
    }
}
