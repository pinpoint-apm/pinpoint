/*
 * Copyright 2020 NAVER Corp.
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

package com.pinpoint.test.plugin;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
public class ElasticSearch8PluginController {

    private final EmbeddedElasticServer elasticServer;

    public ElasticSearch8PluginController(EmbeddedElasticServer elasticServer) {
        this.elasticServer = Objects.requireNonNull(elasticServer, "elasticServer");
    }

    @GetMapping("/index")
    public Mono<String> index() throws Exception {
        // Create the low-level client
        try (RestClient restClient = RestClient.builder(
                new HttpHost(elasticServer.getAddress(), elasticServer.getPort())).build()) {

            // Create the transport with a Jackson mapper
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());

            // And create the API client
            ElasticsearchClient client = new ElasticsearchClient(transport);

            if (client.exists(b -> b.index("products").id("foo")).value()) {
                System.out.println("product exists");
            }

            // Asynchronous non-blocking client
            ElasticsearchAsyncClient asyncClient =
                    new ElasticsearchAsyncClient(transport);

            asyncClient
                    .exists(b -> b.index("products").id("foo"))
                    .thenAccept(response -> {
                        if (response.value()) {
                            System.out.println("product exists");
                        }
                    });
        }

        return Mono.just("OK");
    }
}
