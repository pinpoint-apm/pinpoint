/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.google.httpclient;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * @author jaehong.kim
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-google-httpclient-plugin")
// guava dependency issue
// google-http-client 1.26.0 does not include repackaged guava
@Dependency({ "com.google.http-client:google-http-client:[1.19.0],[1.20.0,1.27.0]", "com.google.guava:guava:20.0",
        WebServer.VERSION, PluginITConstants.VERSION})
public class HttpRequestIT {

    private static WebServer webServer;

    @BeforeClass
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @AfterClass
    public static void AfterClass() throws Exception {
        webServer = WebServer.cleanup(webServer);
    }

    @Test
    public void execute() throws Exception {
        HttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = NET_HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
            }
        });

        GenericUrl url = new GenericUrl(webServer.getCallHttpUrl());
        HttpRequest request = null;
        HttpResponse response = null;
        try {
            request = requestFactory.buildGetRequest(url);
            response = request.execute();
        } catch (IOException ignored) {
        } finally {
            close(response);
        }


        Method executeMethod = HttpRequest.class.getDeclaredMethod("execute");
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTrace(Expectations.event("GOOGLE_HTTP_CLIENT_INTERNAL", executeMethod));
    }

    public void close(HttpResponse response) throws IOException {
        if (response != null) {
            response.disconnect();
        }
    }

    @Test
    public void executeAsync() throws Exception {
        HttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();
        HttpRequestFactory requestFactory = NET_HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
            }
        });
        
        
        GenericUrl url = new GenericUrl(webServer.getCallHttpUrl());
        HttpRequest request = null;
        HttpResponse response = null;
        try {
            request = requestFactory.buildGetRequest(url);
            response = request.executeAsync().get();
        } catch (IOException ignored) {

        } finally {
            close(response);
        }

        Method executeAsyncMethod = HttpRequest.class.getDeclaredMethod("executeAsync", Executor.class);
        Method callMethod = Callable.class.getDeclaredMethod("call");
        Method executeMethod = HttpRequest.class.getDeclaredMethod("execute");
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // async
        verifier.verifyTrace(Expectations.async(Expectations.event("GOOGLE_HTTP_CLIENT_INTERNAL", executeAsyncMethod), Expectations.event("ASYNC", "Asynchronous Invocation")));
    }
}
