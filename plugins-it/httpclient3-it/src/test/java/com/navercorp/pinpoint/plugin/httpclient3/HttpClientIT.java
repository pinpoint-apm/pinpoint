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
package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author jaehong.kim
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-httpclient3-plugin")
@Dependency({ "commons-httpclient:commons-httpclient:[3.0],[3.0.1],[3.1]", WebServer.VERSION, PluginITConstants.VERSION})
public class HttpClientIT {

    private static com.navercorp.pinpoint.pluginit.utils.WebServer webServer;

    // ---------- For @BeforeSharedClass, @AfterSharedClass   //
    private static String CALL_URL;

    public static String getCallUrl() {
        return CALL_URL;
    }

    public static void setCallUrl(String callUrl) {
        CALL_URL = callUrl;
    }
    // ---------- //

    @BeforeSharedClass
    public static void sharedSetUp() throws Exception {
        webServer = WebServer.newTestWebServer();
        setCallUrl(webServer.getCallHttpUrl());
    }

    @AfterSharedClass
    public static void AfterClass() {
        webServer = WebServer.cleanup(webServer);
    }

    private static final long CONNECTION_TIMEOUT = 10000;
    private static final int SO_TIMEOUT = 10000;

    @Test
    public void test() {
        HttpClient client = new HttpClient();
        client.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
        client.getParams().setSoTimeout(SO_TIMEOUT);

        GetMethod method = new GetMethod(getCallUrl());

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("key2", "value2") });

        try {
            // Execute the method.
            client.executeMethod(method);
            assertCaller(method);
        } catch (Exception ignored) {
        } finally {
            method.releaseConnection();
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void hostConfig() {
        HttpClient client = new HttpClient();
        client.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
        client.getParams().setSoTimeout(SO_TIMEOUT);

        HostConfiguration config = new HostConfiguration();
        config.setHost("weather.naver.com", 80, "http");
        GetMethod method = new GetMethod("/rgn/cityWetrMain.nhn");

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("key2", "value2") });

        try {
            // Execute the method.
            client.executeMethod(config, method);
        } catch (Exception ignored) {
        } finally {
            method.releaseConnection();
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }


    private void assertCaller(GetMethod method) {
        final Header[] headers = method.getResponseHeaders(WebServer.CALLER_RESPONSE_HEADER_NAME);
        assertNotNull("caller headers null", headers);
        assertEquals("caller headers count", 1, headers.length);
        final String caller = headers[0].getValue();
        assertNotNull("caller null", caller);
        assertTrue("not caller test", "test".contentEquals(caller));
    }

}