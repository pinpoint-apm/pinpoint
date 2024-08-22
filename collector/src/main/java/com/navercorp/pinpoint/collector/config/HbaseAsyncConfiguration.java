/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.manage.HBaseManager;
import com.navercorp.pinpoint.common.hbase.counter.HBaseBatchPerformance;
import com.navercorp.pinpoint.common.hbase.counter.HbaseBatchPerformanceCounter;
import com.navercorp.pinpoint.common.server.config.CommonCacheManagerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        CommonCacheManagerConfiguration.class,
})
public class HbaseAsyncConfiguration {

    private final Logger logger = LogManager.getLogger(HbaseAsyncConfiguration.class);

    @Bean
    public HbaseBatchPerformanceCounter batchPerformanceCounter() {
        return new HbaseBatchPerformanceCounter();
    }

    @Bean
    public HBaseManager hBaseManager(@Qualifier("batchPerformanceCounter") HBaseBatchPerformance hBaseAsyncOperation) {
        return new HBaseManager(hBaseAsyncOperation);
    }

}
