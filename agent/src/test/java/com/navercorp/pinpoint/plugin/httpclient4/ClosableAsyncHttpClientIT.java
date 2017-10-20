package com.navercorp.pinpoint.plugin.httpclient4;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.navercorp.pinpoint.plugin.WebServer;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author netspider
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({ "org.apache.httpcomponents:httpasyncclient:[4.0,)", "org.nanohttpd:nanohttpd:2.3.1"})
public class ClosableAsyncHttpClientIT {
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
        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().useSystemProperties().build();
        httpClient.start();
        
        try {
            HttpPost httpRequest = new HttpPost(webServer.getCallHttpUrl());
            
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("param1", "value1"));
            httpRequest.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8.name()));
            
            Future<HttpResponse> responseFuture = httpClient.execute(httpRequest, null);
            HttpResponse response = (HttpResponse) responseFuture.get();
            
            if ((response != null) && (response.getEntity() != null)) {
                EntityUtils.consume(response.getEntity());
            }
        } finally {
            httpClient.close();
        }
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", CloseableHttpAsyncClient.class.getMethod("execute", HttpUriRequest.class, FutureCallback.class)));
        final String destinationId = webServer.getHostAndPort();
        final String httpUrl = webServer.getCallHttpUrl();
        verifier.verifyTrace(async(
                    event("HTTP_CLIENT_4", Class.forName("org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl").getMethod("start"), null, null, destinationId,
                            annotation("http.url", httpUrl), annotation("http.entity", "param1=value1")),
                    event("ASYNC","Asynchronous Invocation"),
                    event("HTTP_CLIENT_4_INTERNAL", BasicFuture.class.getMethod("completed", Object.class))
        ));
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", BasicFuture.class.getMethod("get")));
        
        verifier.verifyTraceCount(0);
   }
}