/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.manager.config;

import com.navercorp.pinpoint.common.hbase.AdminFactory;
import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.hbase.manager.ProgramOptions;
import com.navercorp.pinpoint.hbase.schema.dao.SchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.dao.hbase.HbaseSchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.dao.hbase.codec.SchemaChangeLogCodec;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HyunGil Jeong
 */
@Configuration
@ConditionalOnProperty(value = ProgramOptions.DRY_RUN, havingValue = "false", matchIfMissing = true)
public class LiveConfig {

    private final Logger logger = LogManager.getLogger(LiveConfig.class);

    @PostConstruct
    public void init() {
        logger.info("Wired for live run.");
    }

    @Bean
    public HbaseAdminOperation hbaseAdminOperation(AdminFactory adminFactory) {
        return new HBaseAdminTemplate(adminFactory);
    }

    @Bean
    public SchemaChangeLogDao schemaChangeLogDao(HbaseAdminOperation hbaseAdminOperation,
                                                 HbaseOperations hbaseOperations,
                                                 SchemaChangeLogCodec schemaChangeLogCodec) {
        return new HbaseSchemaChangeLogDao(hbaseAdminOperation, hbaseOperations, schemaChangeLogCodec);
    }
}
