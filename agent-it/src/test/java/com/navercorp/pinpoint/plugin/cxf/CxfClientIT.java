package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.plugin.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


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

//        String json = "{\"id\" : 12345, \"name\" : \"victor\"}";
//
//        WebClient client = WebClient.create(address, true);
//
//        ClientConfiguration configuration = WebClient.getConfig(client);
//
//        // add logging interceptor
//        // configuration.getInInterceptors().add(new LoggingInInterceptor());
//        // configuration.getOutInterceptors().add(new LoggingOutInterceptor());
//
//        client.path("/test1").accept("application/json").type("application/json; charset=UTF-8").post(json).close();
//
//        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
//
//        verifier.printCache();
//
//        verifier.ignoreServiceType("JDK_HTTPURLCONNECTOR");
//
//        verifier.verifyTrace(event("CXF_MESSAGE_SENDER", MessageSenderInterceptor.class.getDeclaredMethod("handleMessage", Message.class)));

//        verifier.verifyTrace(event("CXF_LOGGING_OUT", LoggingOutInterceptor.class.getDeclaredMethod("formatLoggingMessage", LoggingMessage.class),
//                annotation("cxf.log.id", "1"),
//                annotation("cxf.address", address + "/test1"),
//                annotation("cxf.http.method", "POST"),
//                annotation("cxf.content.type", "application/json; charset=UTF-8"),
//                annotation("cxf.headers", "{Accept=[application/json], Content-Type=[application/json; charset=UTF-8]}"),
//                annotation("cxf.payload", "{\"id\" : 12345, \"name\" : \"victor\"}")
//        ));
//
//        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
//        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
//
//        verifier.verifyTrace(event("CXF_LOGGING_IN", LoggingInInterceptor.class.getDeclaredMethod("formatLoggingMessage", LoggingMessage.class),
//                annotation("cxf.log.id", "1"),
//                annotation("cxf.response.code", "200"),
//                annotation("cxf.encoding", "ISO-8859-1"),
//                annotation("cxf.content.type", "text/html"),
//                annotation("cxf.headers", "{connection=[keep-alive], Content-Length=[2], content-type=[text/html], Date=[" + gmtFrmt.format(new Date()) + "]}"),
//                annotation("cxf.payload", "{}")
//        ));
//
//        verifier.verifyTraceCount(0);

//        client.close();

    }


}

