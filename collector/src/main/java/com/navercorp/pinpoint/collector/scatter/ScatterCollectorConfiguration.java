/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.scatter;

import com.navercorp.pinpoint.collector.scatter.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.scatter.dao.hbase.HbaseApplicationTraceIndexDao;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.scatter.service",
})
@Configuration
public class ScatterCollectorConfiguration {
    @Bean
    @Primary
    ApplicationTraceIndexDao hbaseApplicationTraceIndexDao(HbasePutWriter putWriter, TableNameProvider tableNameProvider,
                                                           @Qualifier("applicationIndexRowKeyEncoder") RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder) {
        return new HbaseApplicationTraceIndexDao(HbaseTables.APPLICATION_TRACE_INDEX_TRACE, HbaseTables.APPLICATION_TRACE_INDEX_META,
                putWriter, tableNameProvider, applicationIndexRowKeyEncoder);
    }


    @Bean
    ApplicationTraceIndexDao hbaseApplicationTraceIndexDaoV2(HbasePutWriter putWriter, TableNameProvider tableNameProvider,
                                                             @Qualifier("applicationIndexRowKeyEncoderV2") RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder) {
        return new HbaseApplicationTraceIndexDao(HbaseTables.TRACE_INDEX, HbaseTables.TRACE_INDEX_META,
                putWriter, tableNameProvider, applicationIndexRowKeyEncoder);
    }
}
