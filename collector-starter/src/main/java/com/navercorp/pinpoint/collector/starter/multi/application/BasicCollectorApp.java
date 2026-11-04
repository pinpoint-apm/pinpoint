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

package com.navercorp.pinpoint.collector.starter.multi.application;

import com.navercorp.pinpoint.collector.PinpointCollectorModule;
import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import com.navercorp.pinpoint.redis.RedisPropertySources;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        OtlpMetricsExportAutoConfiguration.class
})
@Import({
        PinpointCollectorModule.class,

        MainDataSourcePropertySource.class,
        MainDataSourceConfiguration.class,

        RedisPropertySources.class,
})
public class BasicCollectorApp {
}
