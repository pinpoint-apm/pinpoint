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

package com.navercorp.pinpoint.hbase.manager.config;

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaVerifier;
import com.navercorp.pinpoint.hbase.schema.core.HtdHbaseSchemaVerifier;
import com.navercorp.pinpoint.hbase.schema.dao.SchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.dao.hbase.codec.SchemaChangeLogCodec;
import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaReader;
import com.navercorp.pinpoint.hbase.schema.reader.xml.XmlHbaseSchemaReader;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaService;
import com.navercorp.pinpoint.hbase.schema.service.HbaseSchemaServiceImpl;
import com.navercorp.pinpoint.hbase.schema.service.SchemaChangeLogService;
import com.navercorp.pinpoint.hbase.schema.service.SchemaChangeLogServiceImpl;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HyunGil Jeong
 */
@Configuration
public class AppConfig {

    @Bean
    public HbaseSchemaReader hbaseSchemaReader() {
        return new XmlHbaseSchemaReader();
    }

    @Bean
    public HbaseSchemaVerifier<HTableDescriptor> hbaseSchemaVerifier() {
        return new HtdHbaseSchemaVerifier();
    }

    @Bean
    public SchemaChangeLogCodec schemaChangeLogMapper() {
        return new SchemaChangeLogCodec();
    }

    @Bean
    public SchemaChangeLogService schemaChangeLogService(SchemaChangeLogDao schemaChangeLogDao) {
        return new SchemaChangeLogServiceImpl(schemaChangeLogDao);
    }

    @Bean
    public HbaseSchemaService hbaseSchemaService(HbaseAdminOperation hbaseAdminOperation,
                                                 SchemaChangeLogService schemaChangeLogService,
                                                 HbaseSchemaVerifier<HTableDescriptor> hBaseSchemaVerifier) {
        return new HbaseSchemaServiceImpl(hbaseAdminOperation, schemaChangeLogService, hBaseSchemaVerifier);
    }
}
