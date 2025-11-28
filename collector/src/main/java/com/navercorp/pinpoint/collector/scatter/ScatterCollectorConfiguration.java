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

import com.navercorp.pinpoint.collector.dao.hbase.encode.ApplicationIndexRowKeyEncoder;
import com.navercorp.pinpoint.collector.dao.hbase.encode.TraceIndexRowKeyEncoder;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.scatter.dao",
        "com.navercorp.pinpoint.collector.scatter.service",
})
@Configuration
public class ScatterCollectorConfiguration {

    @Bean
    public RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoder(ApplicationNameRowKeyEncoder rowKeyEncoder,
                                                               @Qualifier("applicationTraceIndexDistributor")
                                                                       RowKeyDistributor rowKeyDistributor) {
        return new ApplicationIndexRowKeyEncoder(rowKeyEncoder, rowKeyDistributor);
    }

    @Bean
    public RowKeyEncoder<SpanBo> traceIndexRowKeyEncoder(@Qualifier("traceIndexDistributor") RowKeyDistributor rowKeyDistributor) {
        return new TraceIndexRowKeyEncoder(rowKeyDistributor);
    }
}
