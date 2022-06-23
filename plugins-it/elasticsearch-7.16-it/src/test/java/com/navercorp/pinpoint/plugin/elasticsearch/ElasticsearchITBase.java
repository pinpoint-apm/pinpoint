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

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * @author Roy Kim
 */

public abstract class ElasticsearchITBase {

    protected static final Logger logger = LogManager.getLogger(ElasticsearchITBase.class);

    protected static int ES_PORT;

    public String getEsHost() {
        return "127.0.0.1";
    }

    public int getEsPort() {
        return ES_PORT;
    }

    public String getEsAddress() {
        return getEsHost() + ":" + ES_PORT;
    }

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        logger.info("ElasticsearchContainer properties:{}", beforeAllResult);

        ES_PORT = Integer.parseInt(beforeAllResult.getProperty("PORT"));
    }

}
