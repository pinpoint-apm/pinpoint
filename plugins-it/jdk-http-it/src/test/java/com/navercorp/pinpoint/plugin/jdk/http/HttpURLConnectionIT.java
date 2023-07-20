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
package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmArgument;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.anyAnnotationValue;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Jongho Moon
 */
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({ WebServer.VERSION, PluginITConstants.VERSION})
@JvmArgument("-Dprofiler.http.record.response.headers=Connection")
public class HttpURLConnectionIT {

    private static WebServer webServer;

    @BeforeAll
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @AfterAll
    public static void AfterClass() throws Exception {
        webServer = WebServer.cleanup(webServer);
    }

    @Test
    public void test() throws Exception {
        URL url = new URL(webServer.getCallHttpUrl());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.getHeaderFields();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printMethod();

        Class<?> targetClass = Class.forName("sun.net.www.protocol.http.HttpURLConnection");
        Method getInputStream = targetClass.getMethod("getInputStream");

        String destinationId = webServer.getHostAndPort();
        String httpUrl = webServer.getCallHttpUrl();
        verifier.verifyTraceCount(2);
        verifier.verifyTrace(event("JDK_HTTPURLCONNECTOR", getInputStream, null, null, destinationId,
                annotation("http.url", httpUrl),
                annotation("http.resp.header", anyAnnotationValue())));
    }

    @Test
    public void testConnectTwice() throws Exception {
        URL url = new URL(webServer.getCallHttpUrl());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.connect();
        connection.getInputStream();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printMethod();

        Class<?> targetClass = Class.forName("sun.net.www.protocol.http.HttpURLConnection");
        Method connect = targetClass.getMethod("connect");
        Method getInputStream = targetClass.getMethod("getInputStream");

        String destinationId = webServer.getHostAndPort();
        String httpUrl = webServer.getCallHttpUrl();
        verifier.verifyTraceCount(2);
        verifier.verifyTrace(event("JDK_HTTPURLCONNECTOR", connect, null, null, destinationId,
                annotation("http.url", httpUrl)));
        verifier.verifyTrace(event("JDK_HTTPURLCONNECTOR", getInputStream, null, null, destinationId,
                annotation("http.url", httpUrl),
                annotation("http.resp.header", anyAnnotationValue())));
    }

    @Test
    public void testConnecting() throws Exception {

        URL url = new URL("http://no.such.url");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        Exception expected1 = null;
        try {
            connection.connect();
        } catch (UnknownHostException e) {
            expected1 = e;
        }

        Exception expected2 = null;
        try {
            connection.connect();
        } catch (UnknownHostException e) {
            expected2 = e;
        }


        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printMethod();

        Class<?> targetClass = Class.forName("sun.net.www.protocol.http.HttpURLConnection");
        Method getInputStream = targetClass.getMethod("connect");

        verifier.verifyTrace(event("JDK_HTTPURLCONNECTOR", getInputStream, expected1, null, null, "no.such.url", annotation("http.url", "http://no.such.url")));
        verifier.verifyTrace(event("JDK_HTTPURLCONNECTOR", getInputStream, expected2, null, null, "no.such.url", annotation("http.url", "http://no.such.url")));

        verifier.printMethod();
        verifier.verifyTraceCount(0);
    }
}
