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

package com.navercorp.pinpoint.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Taejin Koo
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-web:[4.1.2.RELEASE],[4.2.0.RELEASE,4.2.max],[4.3.0.RELEASE,4.3.max]",
        "org.apache.httpcomponents:httpclient:4.3", "io.netty:netty-all:4.1.9.Final",
        WebServer.VERSION, PluginITConstants.VERSION})
@PinpointConfig("pinpoint-disabled-plugin-test.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-resttemplate-plugin"})
public class RestTemplateIT {

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
    public void test1() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(webServer.getCallHttpUrl(), String.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event("REST_TEMPLATE", RestTemplate.class.getConstructor()));
        verifier.verifyTrace(event("REST_TEMPLATE", AbstractClientHttpRequest.class.getMethod("execute"), annotation("http.status.code", 200)));
    }

    @Test
    public void test2() throws Exception {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        String forObject = restTemplate.getForObject(webServer.getCallHttpUrl(), String.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event("REST_TEMPLATE", RestTemplate.class.getConstructor()));
        verifier.verifyTrace(event("REST_TEMPLATE", AbstractClientHttpRequest.class.getMethod("execute"), annotation("http.status.code", 200)));
    }

    @Test
    public void test3() throws Exception {
        RestTemplate restTemplate = new RestTemplate(new Netty4ClientHttpRequestFactory());
        String forObject = restTemplate.getForObject(webServer.getCallHttpUrl(), String.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(event("ASYNC", "Asynchronous Invocation"), 20, 3000);
        verifier.printCache();

        verifier.verifyTrace(event("REST_TEMPLATE", RestTemplate.class.getConstructor()));
        verifier.verifyTrace(event("REST_TEMPLATE", "org.springframework.http.client.AbstractAsyncClientHttpRequest.executeAsync()"));
        verifier.verifyTrace(event("REST_TEMPLATE", "RestTemplate execAsync Result Invocation"));
        verifier.verifyTrace(event("ASYNC", "Asynchronous Invocation"));
        verifier.verifyTrace(event("REST_TEMPLATE", "org.springframework.util.concurrent.SettableListenableFuture.set(java.lang.Object)", annotation("http.status.code", 200)));
    }

}
