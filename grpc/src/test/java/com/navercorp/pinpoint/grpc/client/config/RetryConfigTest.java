package com.navercorp.pinpoint.grpc.client.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.navercorp.pinpoint.grpc.client.retry.HedgingServiceConfigBuilder;
import com.navercorp.pinpoint.grpc.client.retry.RetryServiceConfigBuilder;
import io.grpc.internal.JsonUtil;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


public class RetryConfigTest {

    @Test
    public void retryServiceConfigBuilderTest() {
        Map<String, ?> retryServiceConfig = new RetryServiceConfigBuilder().buildMetadataConfig();
        Map<String, ?> exampleServiceConfig =
                new Gson()
                        .fromJson(
                                new JsonReader(
                                        new InputStreamReader(
                                                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(
                                                        "client/example/retry_service_config.json")),
                                                UTF_8)),
                                Map.class);
        System.out.println(retryServiceConfig);
        System.out.println(exampleServiceConfig);

        Map<String, ?> retryPolicy = getPolicy(retryServiceConfig, "retryPolicy");
        Map<String, ?> examplePolicy = getPolicy(exampleServiceConfig, "retryPolicy");
        for (String key : examplePolicy.keySet()) {
            assertThat(retryPolicy.containsKey(key)).isTrue();
        }
    }

    @Test
    public void hedgeServiceConfigBuilderTest() {
        Map<String, ?> hedgeServiceConfig = new HedgingServiceConfigBuilder().buildMetadataConfig();
        Map<String, ?> exampleServiceConfig =
                new Gson()
                        .fromJson(
                                new JsonReader(
                                        new InputStreamReader(
                                                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(
                                                        "client/example/hedging_service_config.json")),
                                                UTF_8)),
                                Map.class);

        System.out.println(hedgeServiceConfig);
        System.out.println(exampleServiceConfig);

        Map<String, ?> retryPolicy = getPolicy(hedgeServiceConfig, "hedgingPolicy");
        Map<String, ?> examplePolicy = getPolicy(exampleServiceConfig, "hedgingPolicy");
        for (String key : examplePolicy.keySet()) {
            assertThat(retryPolicy.containsKey(key)).isTrue();
        }
    }

    private Map<String, ?> getPolicy(Map<String, ?> serviceConfig, String policy) {
        List<?> methodConfig = JsonUtil.getList(serviceConfig, "methodConfig");
        assertThat(methodConfig).isNotEmpty();
        return JsonUtil.getObject((Map<String, ?>) methodConfig.get(0), policy);
    }
}
