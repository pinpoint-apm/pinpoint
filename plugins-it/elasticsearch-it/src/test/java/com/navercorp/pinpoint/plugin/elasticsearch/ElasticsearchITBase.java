package com.navercorp.pinpoint.plugin.elasticsearch;
/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * @author Roy Kim
 */

public abstract class ElasticsearchITBase {

    protected static final Logger logger = LogManager.getLogger(ElasticsearchITBase.class);

    public static ElasticsearchContainer elasticsearchContainer;


    protected static int SERVER_PORT;
    protected static String SERVER_HOST;
    protected static String ELASTICSEARCH_ADDRESS;

    public static int getServerPort() {
        return SERVER_PORT;
    }

    public static void setServerPort(int serverPort) {
        SERVER_PORT = serverPort;
    }

    public static String getServerHost() {
        return SERVER_HOST;
    }

    public static void setServerHost(String serverHost) {
        SERVER_HOST = serverHost;
    }

    public static String getElasticsearchAddress() {
        return ELASTICSEARCH_ADDRESS;
    }

    public static void setElasticsearchAddress(String elasticsearchAddress) {
        ELASTICSEARCH_ADDRESS = elasticsearchAddress;
    }

    @BeforeSharedClass
    public static void sharedSetUp() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        elasticsearchContainer = ESServerContainerFactory.newESServerContainerFactory(logger.getName());
        elasticsearchContainer.start();

        setServerPort(elasticsearchContainer.getMappedPort(ESServerContainerFactory.DEFAULT_PORT));
        setServerHost(elasticsearchContainer.getHost());
        setElasticsearchAddress(elasticsearchContainer.getHttpHostAddress());

    }

    @AfterSharedClass
    public static void sharedTearDown() {
        if (elasticsearchContainer != null) {
            elasticsearchContainer.stop();
        }
    }


}
