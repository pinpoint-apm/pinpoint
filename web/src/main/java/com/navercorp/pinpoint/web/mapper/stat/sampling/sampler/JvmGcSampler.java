/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class JvmGcSampler implements AgentStatSampler<JvmGcBo, SampledJvmGc> {

    public static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(JvmGcBo.UNCOLLECTED_VALUE);

    @Override
    public SampledJvmGc sampleDataPoints(int timeWindowIndex, long timestamp, List<JvmGcBo> dataPoints, JvmGcBo previousDataPoint) {
        JvmGcType jvmGcType = JvmGcType.UNKNOWN;
        List<Long> heapUseds = new ArrayList<>(dataPoints.size());
        List<Long> heapMaxes = new ArrayList<>(dataPoints.size());
        List<Long> nonHeapUseds = new ArrayList<>(dataPoints.size());
        List<Long> nonHeapMaxes = new ArrayList<>(dataPoints.size());
        List<Long> gcOldCounts = new ArrayList<>(dataPoints.size());
        List<Long> gcOldTimes = new ArrayList<>(dataPoints.size());
        // dataPoints are in descending order
        JvmGcBo previousBo = previousDataPoint;
        for (int i = dataPoints.size() - 1; i >= 0; --i) {
            JvmGcBo jvmGcBo = dataPoints.get(i);
            jvmGcType = jvmGcBo.getGcType();
            if (jvmGcBo.getHeapUsed() != JvmGcBo.UNCOLLECTED_VALUE) {
                heapUseds.add(jvmGcBo.getHeapUsed());
            }
            if (jvmGcBo.getHeapMax() != JvmGcBo.UNCOLLECTED_VALUE) {
                heapMaxes.add(jvmGcBo.getHeapMax());
            }
            if (jvmGcBo.getNonHeapUsed() != JvmGcBo.UNCOLLECTED_VALUE) {
                nonHeapUseds.add(jvmGcBo.getNonHeapUsed());
            }
            if (jvmGcBo.getNonHeapMax() != JvmGcBo.UNCOLLECTED_VALUE) {
                nonHeapMaxes.add(jvmGcBo.getNonHeapMax());
            }

            if (previousBo != null) {
                // Technically, this should not be needed as data should already be partitioned by their agent start
                // timestamp and should only contain data from a single jvm life cycle.
                // Added to maintain backwards compatibility for data that do not have agent start timestamp.
                if (checkJvmRestart(previousBo, jvmGcBo)) {
                    if (isGcCollected(jvmGcBo)) {
                        gcOldCounts.add(jvmGcBo.getGcOldCount());
                        gcOldTimes.add(jvmGcBo.getGcOldTime());
                    } else {
                        jvmGcBo.setGcOldCount(0L);
                        jvmGcBo.setGcOldTime(0L);
                    }
                } else {
                    if (isGcCollected(jvmGcBo) && isGcCollected(previousBo)) {
                        gcOldCounts.add(jvmGcBo.getGcOldCount() - previousBo.getGcOldCount());
                        gcOldTimes.add(jvmGcBo.getGcOldTime() - previousBo.getGcOldTime());
                    } else {
                        if (!isGcCollected(jvmGcBo)) {
                            jvmGcBo.setGcOldCount(previousBo.getGcOldCount());
                            jvmGcBo.setGcOldTime(previousBo.getGcOldTime());
                        }
                    }
                }
            } else {
                if (isGcCollected(jvmGcBo)) {
                    if (timeWindowIndex > 0) {
                        gcOldCounts.add(jvmGcBo.getGcOldCount());
                        gcOldTimes.add(jvmGcBo.getGcOldTime());
                    } else {
                        gcOldCounts.add(0L);
                        gcOldTimes.add(0L);
                    }
                }
            }
            previousBo = jvmGcBo;
        }
        SampledJvmGc sampledJvmGc = new SampledJvmGc();
        sampledJvmGc.setJvmGcType(jvmGcType);
        sampledJvmGc.setHeapUsed(createSampledPoint(timestamp, heapUseds));
        sampledJvmGc.setHeapMax(createSampledPoint(timestamp, heapMaxes));
        sampledJvmGc.setNonHeapUsed(createSampledPoint(timestamp, nonHeapUseds));
        sampledJvmGc.setNonHeapMax(createSampledPoint(timestamp, nonHeapMaxes));
        sampledJvmGc.setGcOldCount(createSampledPoint(timestamp, gcOldCounts));
        sampledJvmGc.setGcOldTime(createSampledPoint(timestamp, gcOldTimes));
        return sampledJvmGc;
    }

    private boolean isGcCollected(JvmGcBo jvmGcBo) {
        return jvmGcBo.getGcOldCount() != JvmGcBo.UNCOLLECTED_VALUE && jvmGcBo.getGcOldTime() != JvmGcBo.UNCOLLECTED_VALUE;
    }

    private boolean checkJvmRestart(JvmGcBo previous, JvmGcBo current) {
        if (previous.getStartTimestamp() > 0 && current.getStartTimestamp() > 0) {
            return previous.getStartTimestamp() != current.getStartTimestamp();
        } else {
            // if start timestamp is not serialzied
            if (current.getGcOldTime() == JvmGcBo.UNCOLLECTED_VALUE || current.getGcOldCount() == JvmGcBo.UNCOLLECTED_VALUE) {
                return false;
            } else {
                long countDelta = current.getGcOldCount() - previous.getGcOldCount();
                long timeDelta = current.getGcOldTime() - previous.getGcOldTime();
                return countDelta < 0 && timeDelta < 0;
            }
        }
    }

    private Point<Long, Long> createSampledPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return new UncollectedPoint<>(timestamp, JvmGcBo.UNCOLLECTED_VALUE);
        } else {
            return new Point<>(
                    timestamp,
                    LONG_DOWN_SAMPLER.sampleMin(values),
                    LONG_DOWN_SAMPLER.sampleMax(values),
                    LONG_DOWN_SAMPLER.sampleAvg(values, 0),
                    LONG_DOWN_SAMPLER.sampleSum(values));
        }
    }
}
