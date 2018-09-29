package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.plugin.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.message.Message;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;


@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.apache.cxf:cxf-rt-rs-client:[3.0.0][3.1.0],[3.2.0,)", "org.nanohttpd:nanohttpd:2.3.1"})
@PinpointConfig("cxf/pinpoint-cxf-test.config")
public class CxfClientIT {

    private static WebServer webServer;

    @BeforeClass
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();

    }

    @AfterClass
    public static void AfterClass() throws Exception {
        final WebServer copy = webServer;
        if (copy != null) {
            copy.stop();
            webServer = null;
        }
    }

    @Test
    public void test() throws Exception {

        String address = webServer.getCallHttpUrl();

        String json = "{\"id\" : 12345, \"name\" : \"victor\"}";

        WebClient client = WebClient.create(address, true);

        client.path("/test1").accept("application/json").type("application/json; charset=UTF-8").post(json).close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        verifier.ignoreServiceType("JDK_HTTPURLCONNECTOR");

        verifier.verifyTrace(event("CXF_CLIENT", MessageSenderInterceptor.MessageSenderEndingInterceptor.class.getDeclaredMethod("handleMessage", Message.class),
                annotation("cxf.http.uri", address + "/test1"),
                annotation("cxf.request.method", "POST"),
                annotation("cxf.content.type", "application/json; charset=UTF-8")
        ));

        verifier.verifyTraceCount(0);
        
        client.close();

    }


}

