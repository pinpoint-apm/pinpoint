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

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author jaehong.kim
 */
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-httpclient3-plugin")
@Dependency({ "commons-httpclient:commons-httpclient:[3.0],[3.0.1],[3.1]", WebServer.VERSION, PluginITConstants.VERSION})
public class HttpClientIT {

    public static WebServer webServer;

    @BeforeAll
    public static void beforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();

    }

    @AfterAll
    public static void afterClass() throws Exception {
        webServer = WebServer.cleanup(webServer);
    }

    public String getAddress() {
        return webServer.getCallHttpUrl();
    }

    public static String getHostPort() {
        return webServer.getHostAndPort();
    }


    private static final long CONNECTION_TIMEOUT = 10000;
    private static final int SO_TIMEOUT = 10000;

    @Test
    public void test() {
        HttpClient client = new HttpClient();
        client.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT);
        client.getParams().setSoTimeout(SO_TIMEOUT);

        GetMethod method = new GetMethod(getAddress());

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setQueryString(new NameValuePair[] { new NameValuePair("key2", "value2") });

        try {
            // Execute the method.
            client.executeMethod(method);
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
        config.setHost("google.com", 80, "http");
        GetMethod method = new GetMethod("/");

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
}