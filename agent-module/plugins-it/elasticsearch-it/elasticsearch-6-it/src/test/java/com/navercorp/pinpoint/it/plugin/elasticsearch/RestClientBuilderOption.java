package com.navercorp.pinpoint.it.plugin.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.util.Objects;

public class RestClientBuilderOption {

    private final String host;
    private final int port;
    private int maxRetryTimeoutMillis = 3000;

    public RestClientBuilderOption(String host, int port) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
    }

    public void setMaxRetryTimeoutMillis(int maxRetryTimeoutMillis) {
        this.maxRetryTimeoutMillis = maxRetryTimeoutMillis;
    }

    public RestClientBuilder build() {
        HttpHost httpHOst = new HttpHost(host, port, "http");
        RestClientBuilder builder = RestClient.builder(httpHOst);
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                requestConfigBuilder
                        .setConnectTimeout(3000)
                        .setSocketTimeout(3000);
                return requestConfigBuilder;
            }
        });
        if (maxRetryTimeoutMillis != 0) {
            // deprecated from 7.0.0
            builder.setMaxRetryTimeoutMillis(maxRetryTimeoutMillis);
        }
        return builder;
    }
}
