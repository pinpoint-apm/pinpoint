package com.pinpointest.plugin.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@RestController
public class JdkHttpPluginController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String HTTP_TARGET = "%s://localhost:%s/helloworld";


    private final SSLContext sslContext;
    private final int httpsPort;
    private final int httpPort;

    public JdkHttpPluginController(@Value("${server.port}") int httpsPort,
                                   @Value("${server.http.port}") int httpPort,
                                   SSLContext sslContext) {
        this.httpsPort = httpsPort;
        this.httpPort = httpPort;
        this.sslContext = Objects.requireNonNull(sslContext, "sslContext");
    }

    private String getHttpUrl(String schema) {
        if (schema.equalsIgnoreCase("https")) {
            return String.format(HTTP_TARGET, schema, httpsPort);
        } else {
            return String.format(HTTP_TARGET, schema, httpPort);
        }
    }


    @GetMapping(value = "/http-get")
    public String httpGet() {
        return httpCall("http", "GET");
    }


    @GetMapping(value = "/https-get")
    public String httpsGet() {
        return httpCall("https", "GET");
    }


    @GetMapping(value = "/http-post")
    public String httpPost() {
        return httpCall("http", "POST");
    }

    @GetMapping(value = "/http-post/already-connect")
    public String httpPostAlreadyConnect() {
        try {
            JdkPostUtils.checkLsmList("http://naver.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    @GetMapping(value = "/https-post")
    public String httpsPost() {
        return httpCall("https", "GET");
    }

    @GetMapping(value = "/https-post/already-connect")
    public String httpsPostAlreadyConnect() {
        try {
            JdkPostUtils.checkLsmList("https://naver.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    @GetMapping(value = "/error")
    public String error() throws IOException {
        try {
            final URL url = new URL("http://localhost:39000");
            HttpURLConnection connection = openConnection(url);
            connection.connect();
        } catch (IOException e) {
            return e.getMessage();
        }
        return "success";
    }

    private String httpCall(String schema, String method) {
        final URL url = newURL(getHttpUrl(schema));
        try {
            HttpURLConnection connection = openConnection(url);
            connection.setRequestMethod(method);
            connection.connect();

            final String contents = readStream(connection);
            final URL targetURL = connection.getURL();

            String log = String.format("url:%s contents:%s", targetURL, contents);
            logger.info(log);
            return log;
        } catch (Exception e) {
            logger.warn("{} open error", url, e);
            return "fail";
        }
    }

    private HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        if (httpURLConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;
            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return "localhost".equalsIgnoreCase(hostname);
                }
            });
        }
        return httpURLConnection;
    }

    private String readStream(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            return IOUtils.toString(inputStream, connection.getContentEncoding());
        }
    }

    private URL newURL(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("invalid url" + spec, exception);
        }
    }

}
