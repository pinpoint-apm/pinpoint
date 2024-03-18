/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.grpc.mapper;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.EachUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.URIKey;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatHistogram;
import org.mapstruct.AfterMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Map;

/**
 * @author intr3p1d
 */
@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
        }
)
public interface UriStatMapper {

    PUriHistogram EMPTY_DETAILED_DATA_INSTANCE = PUriHistogram.getDefaultInstance();
    UriStatHistogramBucket.Layout layout = UriStatHistogramBucket.getLayout();

    @Mappings({
            @Mapping(source = ".", target = "bucketVersion", qualifiedByName = "currentBucketVersion"),
            @Mapping(source = "allUriStatData", target = "eachUriStatList")
    })
    PAgentUriStat map(AgentUriStatData agentUriStatData);

    @Mappings({
            @Mapping(source = "value.uri", target = "uri"),
            @Mapping(source = "value.totalHistogram", target = "totalHistogram", qualifiedByName = "checkEmptyThenMap"),
            @Mapping(source = "value.failedHistogram", target = "failedHistogram", qualifiedByName = "checkEmptyThenMap"),
            @Mapping(source = "key.timestamp", target = "timestamp")
    })
    PEachUriStat map(Map.Entry<URIKey, EachUriStatData> eachUriStatDataEntry);

    @Named("checkEmptyThenMap")
    default PUriHistogram checkEmptyThenMap(UriStatHistogram uriStatHistogram) {
        if (uriStatHistogram.getCount() == 0) {
            return EMPTY_DETAILED_DATA_INSTANCE;
        }
        return map(uriStatHistogram);
    }

    @Mappings({
    })
    PUriHistogram map(UriStatHistogram uriStatHistogram);

    @AfterMapping
    default void map(UriStatHistogram uriStatHistogram, @MappingTarget PUriHistogram.Builder builder) {
        int[] timestampHistograms = uriStatHistogram.getTimestampHistogram();
        for (int eachTimestampHistogram : timestampHistograms) {
            builder.addHistogram(eachTimestampHistogram);
        }
    }

    @Named("currentBucketVersion")
    default int getBucketVersion(AgentUriStatData agentUriStatData) {
        return layout.getBucketVersion();
    }

}
