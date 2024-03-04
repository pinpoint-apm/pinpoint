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

package com.navercorp.pinpoint.batch.configuration;

import com.navercorp.pinpoint.batch.alarm.AlarmCheckerFactory;
import com.navercorp.pinpoint.batch.alarm.CheckerRegistry;
import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.checker.ApdexScoreChecker;
import com.navercorp.pinpoint.batch.alarm.checker.DataSourceConnectionUsageRateChecker;
import com.navercorp.pinpoint.batch.alarm.checker.DeadlockChecker;
import com.navercorp.pinpoint.batch.alarm.checker.ErrorCountChecker;
import com.navercorp.pinpoint.batch.alarm.checker.ErrorCountToCalleeChecker;
import com.navercorp.pinpoint.batch.alarm.checker.ErrorRateChecker;
import com.navercorp.pinpoint.batch.alarm.checker.ErrorRateToCalleeChecker;
import com.navercorp.pinpoint.batch.alarm.checker.FileDescriptorChecker;
import com.navercorp.pinpoint.batch.alarm.checker.HeapUsageRateChecker;
import com.navercorp.pinpoint.batch.alarm.checker.JvmCpuUsageRateChecker;
import com.navercorp.pinpoint.batch.alarm.checker.ResponseCountChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SlowCountToCalleeChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SlowRateChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SlowRateToCalleeChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SystemCpuUsageRateChecker;
import com.navercorp.pinpoint.batch.alarm.checker.TotalCountToCalleeChecker;
import com.navercorp.pinpoint.batch.alarm.collector.AgentEventDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AlarmCheckerConfiguration {

    private final Logger logger = LogManager.getLogger(AlarmCheckerConfiguration.class);

    public AlarmCheckerConfiguration() {
        logger.info("Install AlarmCheckerConfiguration");
    }

    @Bean
    public CheckerRegistry checkerRegistry(List<AlarmCheckerFactory> factories) {
        CheckerRegistry.Builder builder = CheckerRegistry.newBuilder();
        for (AlarmCheckerFactory factory : factories) {
            logger.info("Add AlarmCheckerFactory Category:{} {}", factory.getCategory(), factory.getClass().getSimpleName());
            builder.addChecker(factory);
        }
        return builder.build();
    }


    @Bean
    public AlarmCheckerFactory slowCountChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowCountChecker((ResponseTimeDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.SLOW_COUNT.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory slowRateChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowRateChecker((ResponseTimeDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.SLOW_RATE.name();
            }
        };
    }


    @Bean
    public AlarmCheckerFactory errorCountChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorCountChecker((ResponseTimeDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.ERROR_COUNT.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory errorRateChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorRateChecker((ResponseTimeDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.ERROR_RATE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory totalCountChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ResponseCountChecker((ResponseTimeDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.TOTAL_COUNT.name();
            }
        };
    }


    @Bean
    public AlarmCheckerFactory apdexScoreChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ApdexScoreChecker((ResponseTimeDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.APDEX_SCORE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory slowCountToCalleeChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowCountToCalleeChecker((MapStatisticsCallerDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.SLOW_COUNT_TO_CALLEE.name();
            }
        };
    }


    @Bean
    public AlarmCheckerFactory slowRateToCalleeChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowRateToCalleeChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.SLOW_RATE_TO_CALLEE.name();
            }
        };
    }


    @Bean
    public AlarmCheckerFactory errorCountToCalleeChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorCountToCalleeChecker((MapStatisticsCallerDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.ERROR_COUNT_TO_CALLEE.name();
            }
        };
    }


    @Bean
    public AlarmCheckerFactory errorRateToCalleeChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorRateToCalleeChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.ERROR_RATE_TO_CALLEE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory totalCountToCalleeChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new TotalCountToCalleeChecker((MapStatisticsCallerDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.TOTAL_COUNT_TO_CALLEE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory heapUsageRateChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new HeapUsageRateChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.HEAP_USAGE_RATE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory jvmCpuUsageRateChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new JvmCpuUsageRateChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.JVM_CPU_USAGE_RATE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory systemCpuUsageRateChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SystemCpuUsageRateChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.SYSTEM_CPU_USAGE_RATE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory dataSourceConnectionUsageRateChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new DataSourceConnectionUsageRateChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.DATASOURCE_CONNECTION_USAGE_RATE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory deadlockChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new DeadlockChecker((AgentEventDataCollector) dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.DEADLOCK_OCCURRENCE.name();
            }
        };
    }

    @Bean
    public AlarmCheckerFactory fileDescriptorChecker() {
        return new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new FileDescriptorChecker(dataCollector, rule);
            }

            @Override
            public String getCategory() {
                return CheckerCategory.FILE_DESCRIPTOR_COUNT.name();
            }
        };
    }



}
