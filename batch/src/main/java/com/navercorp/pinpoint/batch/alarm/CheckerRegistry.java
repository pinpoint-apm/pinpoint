package com.navercorp.pinpoint.batch.alarm;


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
import com.navercorp.pinpoint.batch.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.FileDescriptorDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;

import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.HashMap;
import java.util.Map;

public class CheckerRegistry {
    public final Map<CheckerCategory, AlarmCheckerFactory> registry = new HashMap<>();

    public static CheckerRegistry newCheckerRegistry() {
        CheckerRegistry checkerRegistry = new CheckerRegistry();
        checkerRegistry.setup();
        return checkerRegistry;
    }

    private CheckerRegistry() {
    }

    public AlarmCheckerFactory getCheckerFactory(CheckerCategory name) {
        return registry.get(name);
    }

    private void put(CheckerCategory category, AlarmCheckerFactory factory) {
        this.registry.put(category, factory);
    }

    private void setup() {
        put(CheckerCategory.SLOW_COUNT, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowCountChecker((ResponseTimeDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.SLOW_RATE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowRateChecker((ResponseTimeDataCollector) dataCollector, rule);
            }
        });


        put(CheckerCategory.ERROR_COUNT, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorCountChecker((ResponseTimeDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.ERROR_RATE, new AlarmCheckerFactory() {

            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorRateChecker((ResponseTimeDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.TOTAL_COUNT, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ResponseCountChecker((ResponseTimeDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.APDEX_SCORE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ApdexScoreChecker((ResponseTimeDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.SLOW_COUNT_TO_CALLEE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowCountToCalleeChecker((MapStatisticsCallerDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.SLOW_RATE_TO_CALLEE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SlowRateToCalleeChecker(dataCollector, rule);
            }
        });

        put(CheckerCategory.ERROR_COUNT_TO_CALLEE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorCountToCalleeChecker((MapStatisticsCallerDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.ERROR_RATE_TO_CALLEE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new ErrorRateToCalleeChecker(dataCollector, rule);
            }
        });

        put(CheckerCategory.TOTAL_COUNT_TO_CALLEE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new TotalCountToCalleeChecker((MapStatisticsCallerDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.HEAP_USAGE_RATE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new HeapUsageRateChecker((AgentStatDataCollector) dataCollector, rule);
            }
        });

//        put(CheckerCategory.GC_COUNT, new AlarmCheckerFactory() {
//            @Override
//            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
//                return new GcCountChecker((AgentStatDataCollector)dataCollector, rule);
//            }
//        });

        put(CheckerCategory.JVM_CPU_USAGE_RATE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new JvmCpuUsageRateChecker((AgentStatDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.SYSTEM_CPU_USAGE_RATE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new SystemCpuUsageRateChecker((AgentStatDataCollector) dataCollector, rule);
            }
        });

        put(CheckerCategory.DATASOURCE_CONNECTION_USAGE_RATE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new DataSourceConnectionUsageRateChecker((DataSourceDataCollector) dataCollector, rule);
            }
        });
        put(CheckerCategory.DEADLOCK_OCCURRENCE, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new DeadlockChecker((AgentEventDataCollector) dataCollector, rule);
            }
        });
        put(CheckerCategory.FILE_DESCRIPTOR_COUNT, new AlarmCheckerFactory() {
            @Override
            public AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule) {
                return new FileDescriptorChecker((FileDescriptorDataCollector) dataCollector, rule);
            }
        });

    }
}
