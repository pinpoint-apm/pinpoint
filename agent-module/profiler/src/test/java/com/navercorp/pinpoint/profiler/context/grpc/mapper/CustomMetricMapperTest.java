package com.navercorp.pinpoint.profiler.context.grpc.mapper;


import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.DoubleGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongGauge;
import com.navercorp.pinpoint.grpc.trace.PCustomMetric;
import com.navercorp.pinpoint.grpc.trace.PCustomMetricMessage;
import com.navercorp.pinpoint.grpc.trace.PDouleGaugeMetric;
import com.navercorp.pinpoint.grpc.trace.PIntCountMetric;
import com.navercorp.pinpoint.grpc.trace.PIntGaugeMetric;
import com.navercorp.pinpoint.grpc.trace.PLongCountMetric;
import com.navercorp.pinpoint.grpc.trace.PLongGaugeMetric;
import com.navercorp.pinpoint.profiler.context.monitor.metric.DoubleGaugeWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.metric.IntCounterWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.metric.IntGaugeWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.metric.LongCounterWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.metric.LongGaugeWrapper;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.CustomMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.DoubleGaugeMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.IntCountMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.IntGaugeMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.LongCountMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.LongGaugeMetricVo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class CustomMetricMapperTest {

    private static final Random random = new Random();
    private static final int size = 20;
    CustomMetricMapper mapper = new CustomMetricMapperImpl();


    static AgentCustomMetricSnapshotBatch newAgentCustomMetricSnapshotBatch(CustomMetricVo[] customMetricVos) {
        List<AgentCustomMetricSnapshot> agentCustomMetricSnapshots = new ArrayList<>();
        for (CustomMetricVo customMetricVo: customMetricVos) {
            AgentCustomMetricSnapshot agentCustomMetricSnapshot = new AgentCustomMetricSnapshot(1);
            agentCustomMetricSnapshot.add(customMetricVo);
            agentCustomMetricSnapshots.add(agentCustomMetricSnapshot);
        }
        return new AgentCustomMetricSnapshotBatch(agentCustomMetricSnapshots);
    }

    static IntCountMetricVo[] newIntCountMetricVos(String randomString) {
        IntCountMetricVo[] intCountMetricVos = new IntCountMetricVo[size];
        for (int i = 0; i < size; i++) {
            intCountMetricVos[i] = new IntCountMetricVo(
                    new IntCounterWrapper(
                            random.nextInt(),
                            new IntCounter() {
                                final int randomCounter = random.nextInt();

                                @Override
                                public int getValue() {
                                    return randomCounter;
                                }

                                @Override
                                public String getName() {
                                    return randomString;
                                }
                            }
                    )
            );
        }
        return intCountMetricVos;
    }

    static LongCountMetricVo[] newLongCountMetricVos(String randomString) {
        LongCountMetricVo[] longCountMetricVos = new LongCountMetricVo[size];
        for (int i = 0; i < size; i++) {
            longCountMetricVos[i] = new LongCountMetricVo(
                    new LongCounterWrapper(
                            random.nextInt(),
                            new LongCounter() {
                                final long randomCounter = random.nextLong();

                                @Override
                                public long getValue() {
                                    return randomCounter;
                                }

                                @Override
                                public String getName() {
                                    return randomString;
                                }
                            }
                    )
            );
        }
        return longCountMetricVos;
    }

    static IntGaugeMetricVo[] newIntGaugeMetricVos(String randomString) {
        IntGaugeMetricVo[] intGaugeMetricVos = new IntGaugeMetricVo[size];
        for (int i = 0; i < size; i++) {
            intGaugeMetricVos[i] = new IntGaugeMetricVo(
                    new IntGaugeWrapper(
                            random.nextInt(),
                            new IntGauge() {
                                final int randomCounter = random.nextInt();

                                @Override
                                public int getValue() {
                                    return randomCounter;
                                }

                                @Override
                                public String getName() {
                                    return randomString;
                                }
                            }
                    )
            );
        }
        return intGaugeMetricVos;
    }


    static LongGaugeMetricVo[] newLongGaugeMetricVos(String randomString) {
        LongGaugeMetricVo[] longGaugeMetricVos = new LongGaugeMetricVo[size];
        for (int i = 0; i < size; i++) {
            longGaugeMetricVos[i] = new LongGaugeMetricVo(
                    new LongGaugeWrapper(
                            random.nextInt(),
                            new LongGauge() {
                                final long randomCounter = random.nextLong();

                                @Override
                                public long getValue() {
                                    return randomCounter;
                                }

                                @Override
                                public String getName() {
                                    return randomString;
                                }
                            }
                    )
            );
        }
        return longGaugeMetricVos;
    }

    static DoubleGaugeMetricVo[] newDoubleGaugeMetricVos(String randomString) {
        DoubleGaugeMetricVo[] doubleGaugeMetricVos = new DoubleGaugeMetricVo[size];
        for (int i = 0; i < size; i++) {
            doubleGaugeMetricVos[i] = new DoubleGaugeMetricVo(
                    new DoubleGaugeWrapper(
                            random.nextInt(),
                            new DoubleGauge() {
                                final double randomCounter = random.nextDouble();

                                @Override
                                public double getValue() {
                                    return randomCounter;
                                }

                                @Override
                                public String getName() {
                                    return randomString;
                                }
                            }
                    )
            );
        }
        return doubleGaugeMetricVos;
    }


    @Test
    void testIntCountMetric() {
        final String randomString = randomString();
        IntCountMetricVo[] intCountMetricVos = newIntCountMetricVos(randomString);
        AgentCustomMetricSnapshotBatch batch = newAgentCustomMetricSnapshotBatch(intCountMetricVos);
        PCustomMetricMessage pCustomMetricMessage = mapper.map(batch);

        PCustomMetric pCustomMetric = pCustomMetricMessage.getCustomMetrics(0);
        assertEquals(randomString, pCustomMetric.getIntCountMetric().getName());
        assertEquals(intCountMetricVos.length, pCustomMetric.getIntCountMetric().getValuesCount());

        int prevValue = 0;
        for (int i = 0; i < size; i++) {
            assertEquals(
                    intCountMetricVos[i].getValue() - prevValue,
                    pCustomMetric.getIntCountMetric().getValues(i).getValue()
            );
            prevValue = intCountMetricVos[i].getValue();
        }
    }

    @Test
    void testLongCountMetric() {
        final int size = 20;
        final String randomString = randomString();
        LongCountMetricVo[] longCountMetricVos = newLongCountMetricVos(randomString);
        AgentCustomMetricSnapshotBatch batch = newAgentCustomMetricSnapshotBatch(longCountMetricVos);
        PCustomMetricMessage pCustomMetricMessage = mapper.map(batch);

        PCustomMetric pCustomMetric = pCustomMetricMessage.getCustomMetrics(0);
        assertEquals(randomString, pCustomMetric.getLongCountMetric().getName());
        assertEquals(longCountMetricVos.length, pCustomMetric.getLongCountMetric().getValuesCount());

        long prevValue = 0;
        for (int i = 0; i < size; i++) {
            assertEquals(
                    longCountMetricVos[i].getValue() - prevValue,
                    pCustomMetric.getLongCountMetric().getValues(i).getValue()
            );
            prevValue = longCountMetricVos[i].getValue();
        }
    }

    @Test
    void testIntGaugeMetric() {
        final int size = 20;
        final String randomString = randomString();
        IntGaugeMetricVo[] intGaugeMetricVos = newIntGaugeMetricVos(randomString);
        AgentCustomMetricSnapshotBatch batch = newAgentCustomMetricSnapshotBatch(intGaugeMetricVos);
        PCustomMetricMessage pCustomMetricMessage = mapper.map(batch);

        PCustomMetric pCustomMetric = pCustomMetricMessage.getCustomMetrics(0);
        assertEquals(randomString, pCustomMetric.getIntGaugeMetric().getName());
        assertEquals(intGaugeMetricVos.length, pCustomMetric.getIntGaugeMetric().getValuesCount());

        for (int i = 0; i < size; i++) {
            assertEquals(
                    intGaugeMetricVos[i].getValue(),
                    pCustomMetric.getIntGaugeMetric().getValues(i).getValue()
            );
        }
    }



    @Test
    void testLongGaugeMetric() {
        final int size = 20;
        final String randomString = randomString();
        LongGaugeMetricVo[] longGaugeMetricVos = newLongGaugeMetricVos(randomString);
        AgentCustomMetricSnapshotBatch batch = newAgentCustomMetricSnapshotBatch(longGaugeMetricVos);
        PCustomMetricMessage pCustomMetricMessage = mapper.map(batch);

        PCustomMetric pCustomMetric = pCustomMetricMessage.getCustomMetrics(0);
        assertEquals(randomString, pCustomMetric.getLongGaugeMetric().getName());
        assertEquals(longGaugeMetricVos.length, pCustomMetric.getLongGaugeMetric().getValuesCount());

        for (int i = 0; i < size; i++) {
            assertEquals(
                    longGaugeMetricVos[i].getValue(),
                    pCustomMetric.getLongGaugeMetric().getValues(i).getValue()
            );
        }
    }



    @Test
    void testDoubleGaugeMetric() {
        final int size = 20;
        final String randomString = randomString();
        DoubleGaugeMetricVo[] doubleGaugeMetricVos = newDoubleGaugeMetricVos(randomString);
        AgentCustomMetricSnapshotBatch batch = newAgentCustomMetricSnapshotBatch(doubleGaugeMetricVos);
        PCustomMetricMessage pCustomMetricMessage = mapper.map(batch);

        PCustomMetric pCustomMetric = pCustomMetricMessage.getCustomMetrics(0);
        assertEquals(randomString, pCustomMetric.getDoubleGaugeMetric().getName());
        assertEquals(doubleGaugeMetricVos.length, pCustomMetric.getDoubleGaugeMetric().getValuesCount());

        for (int i = 0; i < size; i++) {
            assertEquals(
                    doubleGaugeMetricVos[i].getValue(),
                    pCustomMetric.getDoubleGaugeMetric().getValues(i).getValue()
            );
        }
    }

    @Test
    void testNotSet() {
        final int size = 20;
        final String randomString = randomString();
        DoubleGaugeMetricVo[] notMatchedVos = newDoubleGaugeMetricVos(randomString);
        IntCountMetricVo[] anotherNotMatchedVos = newIntCountMetricVos(randomString);

        PIntCountMetric pIntCount = mapper.createIntCountMetric(randomString, notMatchedVos);
        PLongCountMetric pLongCount = mapper.createLongCountMetric(randomString, notMatchedVos);
        PIntGaugeMetric pIntGauge = mapper.createIntGaugeMetric(randomString, notMatchedVos);
        PLongGaugeMetric pLongGauge = mapper.createLongGaugeMetric(randomString, notMatchedVos);
        PDouleGaugeMetric pDoubleGauge = mapper.createDoubleGaugeMetric(randomString, anotherNotMatchedVos);

        assertEquals(randomString, pIntCount.getName());
        assertEquals(randomString, pLongCount.getName());
        assertEquals(randomString, pIntGauge.getName());
        assertEquals(randomString, pLongGauge.getName());
        assertEquals(randomString, pDoubleGauge.getName());

        assertEquals(size, pIntCount.getValuesCount());
        assertEquals(size, pLongCount.getValuesCount());
        assertEquals(size, pIntGauge.getValuesCount());
        assertEquals(size, pLongGauge.getValuesCount());
        assertEquals(size, pDoubleGauge.getValuesCount());

        for (int i = 0; i < size; i++) {
            assertEquals(Boolean.TRUE, pIntCount.getValues(i).getIsNotSet());
            assertEquals(Boolean.TRUE, pLongCount.getValues(i).getIsNotSet());
            assertEquals(Boolean.TRUE, pIntGauge.getValues(i).getIsNotSet());
            assertEquals(Boolean.TRUE, pLongGauge.getValues(i).getIsNotSet());
            assertEquals(Boolean.TRUE, pDoubleGauge.getValues(i).getIsNotSet());
        }
    }


}