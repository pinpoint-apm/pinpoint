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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.mapper.stat.sampling.AgentStatSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.EagerSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class SampledDataSourceResultExtractor implements ResultsExtractor<List<SampledDataSourceList>> {

    private final TimeWindow timeWindow;
    private final AgentStatMapper<DataSourceListBo> rowMapper;
    private final AgentStatSampler<DataSourceBo, SampledDataSource> sampler;

    public SampledDataSourceResultExtractor(TimeWindow timeWindow, AgentStatMapper<DataSourceListBo> rowMapper, AgentStatSampler<DataSourceBo, SampledDataSource> sampler) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        this.rowMapper = rowMapper;
        this.sampler = sampler;
    }

    @Override
    public List<SampledDataSourceList> extractData(ResultScanner results) throws Exception {
        // divide by dataSource id
        Map<Integer, List<DataSourceBo>> dataSourceBoListMap = divideByDataSourceId(results);

        List<SampledDataSourceList> result = new ArrayList<>(dataSourceBoListMap.size());

        for (List<DataSourceBo> dataSourceBoList : dataSourceBoListMap.values()) {
            result.add(getSampleData(dataSourceBoList));
        }

        return result;
    }

    private Map<Integer, List<DataSourceBo>> divideByDataSourceId(ResultScanner results) throws Exception {
        int rowNum = 0;
        Map<Integer, List<DataSourceBo>> dataSourceBoListMap = new HashMap<>();
        for (Result result : results) {
            for (DataSourceListBo dataPoint : this.rowMapper.mapRow(result, rowNum++)) {
                if (dataPoint.size() == 0) {
                    continue;
                }

                DataSourceBo first = ListUtils.getFirst(dataPoint.getList(), null);
                int id = first.getId();

                List<DataSourceBo> dataSourceBoList = dataSourceBoListMap.computeIfAbsent(id, k -> new ArrayList<>());

                dataSourceBoList.addAll(dataPoint.getList());
            }
        }
        return dataSourceBoListMap;
    }

    private SampledDataSourceList getSampleData(List<DataSourceBo> dataSourceBoList) {
        dataSourceBoList.sort(new Comparator<DataSourceBo>() {
            @Override
            public int compare(DataSourceBo o1, DataSourceBo o2) {
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
            }
        });

        AgentStatSamplingHandler<DataSourceBo, SampledDataSource> samplingHandler = new EagerSamplingHandler<>(timeWindow, sampler);
        for (DataSourceBo dataSourceBo : dataSourceBoList) {
            samplingHandler.addDataPoint(dataSourceBo);
        }
        List<SampledDataSource> sampledDataSources = samplingHandler.getSampledDataPoints();

        SampledDataSourceList sampledDataSourceList = new SampledDataSourceList();
        for (SampledDataSource sampledDataSource : sampledDataSources) {
            sampledDataSourceList.addSampledDataSource(sampledDataSource);
        }

        return sampledDataSourceList;
    }

}
