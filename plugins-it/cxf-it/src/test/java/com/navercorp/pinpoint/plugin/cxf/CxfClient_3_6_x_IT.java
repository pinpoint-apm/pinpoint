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

package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PinpointAgent(AgentPath.PATH)
@JvmVersion(11)
@Dependency({"org.apache.cxf:cxf-rt-rs-client:[3.6.0,3.max)", WebServer.VERSION, PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-cxf-plugin", "com.navercorp.pinpoint:pinpoint-jdk-http-plugin"})
@PinpointConfig("cxf/pinpoint-cxf-test.config")
public class CxfClient_3_6_x_IT {

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

    @Test
    public void test() throws Exception {

        String address = getAddress();

        String json = "{\"id\" : 12345, \"name\" : \"victor\"}";

        WebClient client = WebClient.create(address, true);

        ClientConfiguration configuration = WebClient.getConfig(client);

        // add logging interceptor
        configuration.getInInterceptors().add(new LoggingInInterceptor());
        configuration.getOutInterceptors().add(new LoggingOutInterceptor());

        client.path("/test1").accept("application/json").type("application/json; charset=UTF-8").post(json).close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        verifier.ignoreServiceType("JDK_HTTPURLCONNECTOR");

        verifier.verifyTrace(event("CXF_MESSAGE_SENDER", MessageSenderInterceptor.class.getDeclaredMethod("handleMessage", Message.class)));

        verifier.verifyTrace(event("CXF_LOGGING_OUT", LoggingOutInterceptor.class.getDeclaredMethod("formatLoggingMessage", LoggingMessage.class),
                annotation("cxf.address", address + "/test1"),
                annotation("cxf.http.method", "POST"),
                annotation("cxf.content.type", "application/json; charset=UTF-8"),
                annotation("cxf.headers", "{Accept=[application/json], Content-Type=[application/json; charset=UTF-8]}"),
                annotation("cxf.payload", "{\"id\" : 12345, \"name\" : \"victor\"}")
        ));

//        verifier.verifyTrace(event("CXF_LOGGING_IN", LoggingInInterceptor.class.getDeclaredMethod("formatLoggingMessage", LoggingMessage.class),
//                annotation("cxf.response.code", "200"),
//                annotation("cxf.encoding", "ISO-8859-1"),
//                annotation("cxf.content.type", "text/html"),
//                annotation("cxf.headers", "{connection=[keep-alive], Content-Length=[2], content-type=[text/html], Date=[" + new Date() + "]}"),
//                annotation("cxf.payload", "{}")
//        ));

        verifier.verifyTraceCount(1);

        client.close();

    }


}
