/*
 * Copyright 2026 NAVER Corp.
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
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AutoClose;
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
 * okhttp 4.4 moved RealCall to okhttp3.internal.connection ([4.0, 4.3] keeps okhttp3.RealCall),
 * and 4.x replaced the NamedRunnable.execute() template method of AsyncCall with run().
 * Dispatcher.enqueue is a kotlin internal fun, compiled as enqueue$okhttp.
 */
public abstract class OkHttpClient_4_x_BaseIT {
    static final String ASYNC = "ASYNC";
    static final String OK_HTTP_CLIENT = "OK_HTTP_CLIENT";
    static final String OK_HTTP_CLIENT_INTERNAL = "OK_HTTP_CLIENT_INTERNAL";

    @AutoClose("stop")
    private static WebServer webServer;

    @BeforeAll
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @Test
    public void execute() throws Exception {
        Request request = new Request.Builder().url(webServer.getCallHttpUrl()).build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method executeMethod = realCallClass().getDeclaredMethod("execute");
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, executeMethod));

        verifyBridgeInterceptor(verifier, request, response);
        verifyConnectIfPresent(verifier, request);

        verifier.verifyTraceCount(0);
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
            public void onResponse(Call call, Response response) {
                responseRef.set(response);
                latch.countDown();
            }
        });
        latch.await(3, TimeUnit.SECONDS);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(event(ASYNC, "Asynchronous Invocation"), 20, 3000);
        verifier.printCache();

        Class<?> realCallClass = realCallClass();
        Class<?> asyncCallClass = Class.forName(realCallClass.getName() + "$AsyncCall");

        Method realCallEnqueueMethod = realCallClass.getDeclaredMethod("enqueue", Class.forName("okhttp3.Callback"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, realCallEnqueueMethod));

        Method dispatcherEnqueueMethod = getDispatcherEnqueueMethod(asyncCallClass);
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, dispatcherEnqueueMethod));

        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));

        Method asyncCallMethod = getAsyncCallMethod(asyncCallClass);
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, asyncCallMethod));

        verifyBridgeInterceptor(verifier, request, responseRef.get());
        verifyConnectIfPresent(verifier, request);

        verifier.verifyTraceCount(0);
    }

    private void verifyBridgeInterceptor(PluginTestVerifier verifier, Request request, Response response) throws Exception {
        Method interceptMethod = Class.forName("okhttp3.internal.http.BridgeInterceptor").getDeclaredMethod("intercept", Class.forName("okhttp3.Interceptor$Chain"));
        verifier.verifyTrace(event(OK_HTTP_CLIENT, interceptMethod,
                null, null, webServer.getHostAndPort(),
                annotation("http.url", request.url().toString()),
                annotation("http.status.code", response.code()))
        );
    }

    private void verifyConnectIfPresent(PluginTestVerifier verifier, Request request) throws Exception {
        Method connectMethod = getConnectMethod();
        if (connectMethod == null) {
            // 5.x moved connection establishment to okhttp3.internal.connection.ConnectPlan
            return;
        }
        String hostAndPort = toHostAndPortString(request.url().host(), request.url().port());
        verifier.verifyTrace(event(OK_HTTP_CLIENT_INTERNAL, connectMethod,
                annotation("http.internal.display", hostAndPort)));
    }

    private static Class<?> realCallClass() throws ClassNotFoundException {
        // 4.4 +
        try {
            return Class.forName("okhttp3.internal.connection.RealCall");
        } catch (ClassNotFoundException e) {
            // [4.0, 4.3]
            return Class.forName("okhttp3.RealCall");
        }
    }

    private static Method getDispatcherEnqueueMethod(Class<?> asyncCallClass) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> dispatcherClass = Class.forName("okhttp3.Dispatcher");
        try {
            return dispatcherClass.getDeclaredMethod("enqueue", asyncCallClass);
        } catch (NoSuchMethodException e) {
            // 4.x, 5.x - kotlin `internal fun enqueue` is compiled as enqueue$okhttp
            return dispatcherClass.getDeclaredMethod("enqueue$okhttp", asyncCallClass);
        }
    }

    private static Method getAsyncCallMethod(Class<?> asyncCallClass) throws NoSuchMethodException {
        // mirrors the plugin: NamedRunnable.execute() when present, run() otherwise
        try {
            return asyncCallClass.getDeclaredMethod("execute");
        } catch (NoSuchMethodException e) {
            return asyncCallClass.getDeclaredMethod("run");
        }
    }

    private Method getConnectMethod() throws ClassNotFoundException {
        // 4.x
        try {
            return Class.forName("okhttp3.internal.connection.RealConnection").getDeclaredMethod("connect", int.class, int.class, int.class, int.class, boolean.class,
                    Class.forName("okhttp3.Call"), Class.forName("okhttp3.EventListener"));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static String toHostAndPortString(String host, int port) {
        if (port == -1) {
            return host;
        }
        return host + ':' + port;
    }
}
