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

package com.navercorp.pinpoint.collector.applicationmap.statistics.config;

import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        BulkProperties.class,

})
public class BulkConfiguration {

    @Bean
    public BulkIncrementerFactory bulkIncrementerFactory() {
        return new BulkIncrementerFactory();
    }

    @Bean
    public BulkOperationReporterFactory bulkOperationReporterFactory() {
        return new BulkOperationReporterFactory();
    }

    @Bean
    public BulkFactory bulkFactory(BulkProperties bulkProperties,
                                   HbaseAsyncTemplate asyncTemplate,
                                   BulkIncrementerFactory bulkIncrementerFactory,
                                   BulkOperationReporterFactory bulkOperationReporterFactory) {
        return new BulkFactory(bulkProperties.enableBulk(), asyncTemplate, bulkIncrementerFactory, bulkOperationReporterFactory);
    }

}
