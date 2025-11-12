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

package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentResponse;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapAgentResponseTimeDao implements MapAgentResponseDao {

    private static final int NUM_PARTITIONS = 8;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;

    private final ResultExtractorFactory<List<ResponseTime>> resultExtractFactory;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final MapScanFactory scanFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;

    public HbaseMapAgentResponseTimeDao(HbaseColumnFamily table,
                                        HbaseOperations hbaseOperations,
                                        TableNameProvider tableNameProvider,
                                        ResultExtractorFactory<List<ResponseTime>> resultExtractMapperFactory,
                                        MapScanFactory scanFactory,
                                        RowKeyDistributorByHashPrefix rowKeyDistributor) {
        this.table = Objects.requireNonNull(table, "table");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.resultExtractFactory = Objects.requireNonNull(resultExtractMapperFactory, "resultExtractMapperFactory");

        this.scanFactory = Objects.requireNonNull(scanFactory, "scanFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }


    @Override
    public List<ResponseTime> selectResponseTime(Application application, TimeWindow timeWindow) {
        Objects.requireNonNull(application, "application");

        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, timeWindow);
        }

        Range windowRange = timeWindow.getWindowRange();
        Scan scan = scanFactory.createScan("MapSelfScan", ServiceUid.DEFAULT_SERVICE_UID_CODE, application, windowRange, table.getName());

        ResultsExtractor<List<ResponseTime>> resultsExtractor = resultExtractFactory.newMapper(timeWindow);

        TableName mapStatisticsSelfTableName = tableNameProvider.getTableName(table.getTable());
        List<ResponseTime> responseTimeList = hbaseOperations.findParallel(mapStatisticsSelfTableName, scan, rowKeyDistributor,
                resultsExtractor, NUM_PARTITIONS);

        if (responseTimeList.isEmpty()) {
            return List.of();
        }
        return responseTimeList;
    }

    @Override
    public AgentResponse selectAgentResponse(Application application, TimeWindow timeWindow) {
        Objects.requireNonNull(application, "application");

        if (logger.isDebugEnabled()) {
            logger.debug("selectAgentResponse applicationName:{}, {}", application, timeWindow);
        }

        List<ResponseTime> responseTimes = selectResponseTime(application, timeWindow);
        if (responseTimes.isEmpty()) {
            return AgentResponse.newBuilder(application).build();
        }
        AgentResponse.Builder builder = AgentResponse.newBuilder(application);
        builder.addAgentResponse(responseTimes);
        return builder.build();
    }

    @Override
    public Set<String> selectAgentIds(Application application, TimeWindow timeWindow) {
        List<ResponseTime> responseTimes = selectResponseTime(application, timeWindow);
        Set<String> agentIds = new HashSet<>();
        for (ResponseTime responseTime : responseTimes) {
            agentIds.addAll(responseTime.getAgentIds());
        }
        return agentIds;
    }

}