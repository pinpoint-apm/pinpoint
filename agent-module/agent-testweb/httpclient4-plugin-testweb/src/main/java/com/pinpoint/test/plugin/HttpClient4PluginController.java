package com.pinpoint.test.plugin;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class HttpClient4PluginController {

    @GetMapping("/")
    public Mono<String> weclome() {
        return Mono.just("Welcome");
    }

    @GetMapping("/response")
    public String response() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://naver.com/");
            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            return responseBody;
        } finally {
            httpclient.close();
        }
    }

    @GetMapping("/release")
    public String release() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://httpbin.org/get");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to bother about connection release
                if (entity != null) {
                    InputStream inStream = entity.getContent();
                    try {
                        int c;
                        StringBuilder sb = new StringBuilder();
                        while ((c = inStream.read()) != -1) {
                            sb.append(c);
                        }
                        return sb.toString();
                    } catch (IOException ex) {
                        // In case of an IOException the connection will be released
                        // back to the connection manager automatically
                        throw ex;
                    } finally {
                        // Closing the input stream will trigger connection release
                        inStream.close();
                    }
                }
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }

        return "OK";
    }

    @GetMapping("/abort")
    public String abort() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://httpbin.org/get");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                StatusLine statusLine = response.getStatusLine();
                // Do not feel like reading the response body
                // Call abort on the request object
                httpget.abort();
                return statusLine.getReasonPhrase();
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    @RequestMapping(value = "/cookie")
    public String cookie(@RequestHeader(value = "Cookie", required = false) String cookie) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://localhost:9999/unknown");
            httpget.addHeader("Cookie", "foo-bar");
            CloseableHttpResponse response = httpclient.execute(httpget);
            return response.toString();
        } finally {
            httpclient.close();
        }
    }
}