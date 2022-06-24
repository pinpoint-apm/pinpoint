/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.monitor.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetric;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.DoubleGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongGauge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class CustomMetricRegistryServiceTest {

    private static final String DEFAULT_TEST_METRIC_NAME = "groupName/metricName/label";

    private final Random random = new Random(System.currentTimeMillis());

    @Test
    public void intCountMetricTest() {
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(10, new SkipFilter());

        int value = random.nextInt(100);

        CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, DEFAULT_TEST_METRIC_NAME, value, IntCounter.class);
        Assertions.assertNotNull(customMetric);

        Map<String, CustomMetricWrapper> customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(1, customMetricMap.size());

        CustomMetricWrapper customMetricWrapper = customMetricMap.get(DEFAULT_TEST_METRIC_NAME);
        Assertions.assertTrue(customMetricWrapper instanceof IntCounterWrapper);

        IntCounterWrapper intCountMetricWrapper = (IntCounterWrapper) customMetricWrapper;
        Assertions.assertEquals(value, intCountMetricWrapper.getValue());

        boolean unregister = customMetricRegistryService.unregister((IntCounter) customMetric);
        Assertions.assertTrue(unregister);

        customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(0, customMetricMap.size());
    }

    @Test
    public void longCountMetricTest() {
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(10, new SkipFilter());

        long value = random.nextLong();

        CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, DEFAULT_TEST_METRIC_NAME, value, LongCounter.class);
        Assertions.assertNotNull(customMetric);

        Map<String, CustomMetricWrapper> customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(1, customMetricMap.size());

        CustomMetricWrapper customMetricWrapper = customMetricMap.get(DEFAULT_TEST_METRIC_NAME);
        Assertions.assertTrue(customMetricWrapper instanceof LongCounterWrapper);

        LongCounterWrapper longCountMetricWrapper = (LongCounterWrapper) customMetricWrapper;
        Assertions.assertEquals(value, longCountMetricWrapper.getValue());

        boolean unregister = customMetricRegistryService.unregister((LongCounter) customMetric);
        Assertions.assertTrue(unregister);

        customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(0, customMetricMap.size());
    }


    @Test
    public void intGaugeMetricTest() {
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(10, new SkipFilter());

        int value = random.nextInt(100);

        CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, DEFAULT_TEST_METRIC_NAME, value, IntGauge.class);
        Assertions.assertNotNull(customMetric);

        Map<String, CustomMetricWrapper> customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(1, customMetricMap.size());

        CustomMetricWrapper customMetricWrapper = customMetricMap.get(DEFAULT_TEST_METRIC_NAME);
        Assertions.assertTrue(customMetricWrapper instanceof IntGaugeWrapper);

        IntGaugeWrapper intGaugeMetricWrapper = (IntGaugeWrapper) customMetricWrapper;
        Assertions.assertEquals(value, intGaugeMetricWrapper.getValue());

        boolean unregister = customMetricRegistryService.unregister((IntGauge) customMetric);
        Assertions.assertTrue(unregister);

        customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(0, customMetricMap.size());
    }


    @Test
    public void longGaugeMetricTest() {
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(10, new SkipFilter());

        long value = random.nextLong();

        CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, DEFAULT_TEST_METRIC_NAME, value, LongGauge.class);
        Assertions.assertNotNull(customMetric);

        Map<String, CustomMetricWrapper> customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(1, customMetricMap.size());

        CustomMetricWrapper customMetricWrapper = customMetricMap.get(DEFAULT_TEST_METRIC_NAME);
        Assertions.assertTrue(customMetricWrapper instanceof LongGaugeWrapper);

        LongGaugeWrapper longGaugeMetricWrapper = (LongGaugeWrapper) customMetricWrapper;
        Assertions.assertEquals(value, longGaugeMetricWrapper.getValue());

        boolean unregister = customMetricRegistryService.unregister((LongGauge) customMetric);
        Assertions.assertTrue(unregister);

        customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(0, customMetricMap.size());
    }

    @Test
    public void doubleGaugeMetricTest() {
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(10, new SkipFilter());

        double value = random.nextDouble();

        CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, DEFAULT_TEST_METRIC_NAME, value, DoubleGauge.class);
        Assertions.assertNotNull(customMetric);

        Map<String, CustomMetricWrapper> customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(1, customMetricMap.size());

        CustomMetricWrapper customMetricWrapper = customMetricMap.get(DEFAULT_TEST_METRIC_NAME);
        Assertions.assertTrue(customMetricWrapper instanceof DoubleGaugeWrapper);

        DoubleGaugeWrapper doubleGaugeMetricWrapper = (DoubleGaugeWrapper) customMetricWrapper;
        Assertions.assertEquals(value, doubleGaugeMetricWrapper.getValue(), 0.0);

        boolean unregister = customMetricRegistryService.unregister((DoubleGauge) customMetric);
        Assertions.assertTrue(unregister);

        customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(0, customMetricMap.size());
    }

    @Test
    public void mixedCustomMetricTest() {
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(10, new SkipFilter());

        int intValue = random.nextInt(100);
        final String intMetricName = "groupName/metricName/int";
        CustomMetric intCountMetric = createAndAddFixedValueMetric(customMetricRegistryService, intMetricName, intValue, IntCounter.class);
        Assertions.assertNotNull(intCountMetric);

        long longValue = random.nextLong();
        final String longMetricName = "groupName/metricName/long";
        CustomMetric longGaugeMetric = createAndAddFixedValueMetric(customMetricRegistryService, longMetricName, longValue, LongGauge.class);
        Assertions.assertNotNull(longGaugeMetric);

        Map<String, CustomMetricWrapper> customMetricMap = customMetricRegistryService.getCustomMetricMap();
        Assertions.assertEquals(2, customMetricMap.size());

        IntCounterWrapper intCountMetricWrapper = (IntCounterWrapper) customMetricMap.get(intMetricName);
        Assertions.assertEquals(intValue, intCountMetricWrapper.getValue());

        LongGaugeWrapper longGaugeMetricWrapper = (LongGaugeWrapper) customMetricMap.get(longMetricName);
        Assertions.assertEquals(longValue, longGaugeMetricWrapper.getValue());

        customMetricRegistryService.unregister((IntCounter) intCountMetric);
        Assertions.assertEquals(1, customMetricMap.size());

        customMetricRegistryService.unregister((LongGauge) longGaugeMetric);
        Assertions.assertEquals(0, customMetricMap.size());
    }

    @Test
    public void limitNumberMetricTest() {
        int limitIdNumber = 10;
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(limitIdNumber, new SkipFilter());

        int value = random.nextInt(100);

        for (int i = 0; i < limitIdNumber + 10; i++) {
            CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, "groupName/metricName/label" + i, value, DoubleGauge.class);
            if (i < limitIdNumber) {
                Assertions.assertNotNull(customMetric);
            } else {
                Assertions.assertNull(customMetric);
            }
        }
    }

    @Test
    public void illegalMetricNameTest() {
        int limitIdNumber = 10;
        CustomMetricRegistryService customMetricRegistryService = new DefaultCustomMetricRegistryService(limitIdNumber, new SkipFilter());

        int value = random.nextInt(100);
        CustomMetric customMetric = createAndAddFixedValueMetric(customMetricRegistryService, "abcde", value, DoubleGauge.class);
        Assertions.assertNull(customMetric);

        customMetric = createAndAddFixedValueMetric(customMetricRegistryService, "abcde/abcde", value, DoubleGauge.class);
        Assertions.assertNull(customMetric);

        customMetric = createAndAddFixedValueMetric(customMetricRegistryService, "a@bcd@e/abcde/abcde", value, DoubleGauge.class);
        Assertions.assertNull(customMetric);

        customMetric = createAndAddFixedValueMetric(customMetricRegistryService, "a@bcd@e//abcde", value, DoubleGauge.class);
        Assertions.assertNull(customMetric);
    }


    private CustomMetric createAndAddFixedValueMetric(CustomMetricRegistryService customMetricRegistryService, final String metricName, final Number fixedValue, Class clazz) {
        CustomMetric fixedValueMetric = createFixedValueMetric(metricName, fixedValue, clazz);
        boolean register = false;
        if (clazz == IntCounter.class) {
            register = customMetricRegistryService.register((IntCounter) fixedValueMetric);
        } else if (clazz == LongCounter.class) {
            register = customMetricRegistryService.register((LongCounter) fixedValueMetric);
        } else if (clazz == IntGauge.class) {
            register = customMetricRegistryService.register((IntGauge) fixedValueMetric);
        } else if (clazz == LongGauge.class) {
            register = customMetricRegistryService.register((LongGauge) fixedValueMetric);
        } else if (clazz == DoubleGauge.class) {
            register = customMetricRegistryService.register((DoubleGauge) fixedValueMetric);
        } else {
            throw new IllegalArgumentException("unsupported clazz");
        }

        if (register) {
            return fixedValueMetric;
        } else {
            return null;
        }
    }

    //
    private CustomMetric createFixedValueMetric(final String metricName, final Number fixedValue, Class<?> clazz) {
        if (clazz == IntCounter.class) {
            return new IntCounter() {
                @Override
                public int getValue() {
                    return (Integer) fixedValue;
                }

                @Override
                public String getName() {
                    return metricName;
                }
            };
        }
        if (clazz == LongCounter.class) {
            return new LongCounter() {
                @Override
                public long getValue() {
                    return (Long) fixedValue;
                }

                @Override
                public String getName() {
                    return metricName;
                }
            };
        }
        if (clazz == IntGauge.class) {
            return new IntGauge() {
                @Override
                public int getValue() {
                    return (Integer) fixedValue;
                }

                @Override
                public String getName() {
                    return metricName;
                }
            };
        }
        if (clazz == LongGauge.class) {
            return new LongGauge() {
                @Override
                public long getValue() {
                    return (Long) fixedValue;
                }

                @Override
                public String getName() {
                    return metricName;
                }
            };
        }
        if (clazz == DoubleGauge.class) {
            return new DoubleGauge() {
                @Override
                public double getValue() {
                    return (Double) fixedValue;
                }

                @Override
                public String getName() {
                    return metricName;
                }
            };
        }
        throw new IllegalArgumentException("unsupported clazz");
    }

    private static class SkipFilter implements CustomMetricRegistryFilter {

        @Override
        public boolean filter(CustomMetric value) {
            return NOT_FILTERED;
        }
    }

}
