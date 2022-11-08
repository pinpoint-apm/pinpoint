/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.sampler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.util.MapUtils;
import com.navercorp.pinpoint.profiler.sampler.CountingSamplerFactory;
import com.navercorp.pinpoint.profiler.sampler.PercentRateSampler;
import com.navercorp.pinpoint.profiler.sampler.PercentSamplerFactory;
import com.navercorp.pinpoint.profiler.sampler.SamplerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlSamplerConfig {
    private static final String PREFIX = "profiler.sampling.url.";
    private static final String PATH = ".path";
    private static final String COUNTING_SAMPLING_RATE = ".counting.sampling-rate";
    private static final String PERCENT_SAMPLING_RATE = ".percent.sampling-rate";
    private static final String NEW_THROUGHPUT = ".new.throughput";
    private static final String CONTINUE_THROUGHPUT = ".continue.throughput";

    private static final String PATTERN_REGEX = PREFIX + "[0-9]+" + "(" + PATH + "|" + COUNTING_SAMPLING_RATE + "|" + PERCENT_SAMPLING_RATE + "|" + NEW_THROUGHPUT + "|" + CONTINUE_THROUGHPUT + ")";

    private ProfilerConfig config;
    private SamplerType samplerType;

    public UrlSamplerConfig(ProfilerConfig config, SamplerType samplerType) {
        this.config = config;
        this.samplerType = samplerType;
    }

    public List<Map.Entry<Integer, UrlSamplerInfo>> entryList() {
        final Map<Integer, UrlSamplerInfo> result = new HashMap<>();
        final Map<String, String> patterns = config.readPattern(PATTERN_REGEX);
        if (MapUtils.isEmpty(patterns)) {
            return Collections.emptyList();
        }

        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            final String key = entry.getKey();
            if (key == null || !key.startsWith(PREFIX)) {
                continue;
            }
            final int point = key.indexOf('.', PREFIX.length());
            if (point < 0) {
                // not found element
                continue;
            }

            final int number = NumberUtils.parseInteger(key.substring(PREFIX.length(), point), -1);
            if (number == -1) {
                // invalid number
                continue;
            }

            UrlSamplerInfo target = result.get(number);
            if (target == null) {
                target = new UrlSamplerInfo();
                result.put(number, target);
            }

            if (key.endsWith(PATH)) {
                final String urlPath = entry.getValue();
                target.setUrlPath(urlPath);
            } else if (key.endsWith(COUNTING_SAMPLING_RATE)) {
                if (samplerType == SamplerType.COUNTING) {
                    int samplingRate = NumberUtils.parseInteger(entry.getValue(), -1);
                    final CountingSamplerFactory factory = new CountingSamplerFactory(samplingRate);
                    target.setSampler(factory.createSampler());
                }
            } else if (key.endsWith(PERCENT_SAMPLING_RATE)) {
                if (samplerType == SamplerType.PERCENT) {
                    double samplingRateDouble = NumberUtils.parseDouble(entry.getValue(), 100);
                    long samplingRate = (long) (samplingRateDouble * PercentRateSampler.MULTIPLIER);
                    final PercentSamplerFactory factory = new PercentSamplerFactory(samplingRate);
                    target.setSampler(factory.createSampler());
                }
            } else if (key.endsWith(NEW_THROUGHPUT)) {
                int samplingNewThroughput = NumberUtils.parseInteger(entry.getValue(), 0);
                target.setSamplingNewThroughput(samplingNewThroughput);
            } else if (key.endsWith(CONTINUE_THROUGHPUT)) {
                int samplingContinueThroughput = NumberUtils.parseInteger(entry.getValue(), 0);
                target.setSamplingContinueThroughput(samplingContinueThroughput);
            }
        }

        // sort by number
        final List<Map.Entry<Integer, UrlSamplerInfo>> entries = new ArrayList<>(result.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getKey));

        return entries;
    }
}
