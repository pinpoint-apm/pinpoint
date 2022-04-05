package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractHBaseDao<T extends AgentStatDataPoint> implements AgentStatDaoV2<T> {

    private final AgentStatType agentStatType;
    private final HbaseTable tableName;
    private final Function<AgentStatBo, List<T>> dataPointFunction;
    private final HbaseOperations2 hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final AgentStatHbaseOperationFactory operationFactory;
    private final AgentStatSerializer<T> serializer;

    protected Function<List<T>, List<T>> preprocessor = Function.identity();

    public AbstractHBaseDao(AgentStatType agentStatType,
                            HbaseTable tableName,
                            Function<AgentStatBo, List<T>> dataPointFunction,
                            HbaseOperations2 hbaseTemplate,
                            TableNameProvider tableNameProvider,
                            AgentStatHbaseOperationFactory operationFactory,
                            AgentStatSerializer<T> serializer) {
        this.agentStatType = Objects.requireNonNull(agentStatType, "agentStatType");
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.dataPointFunction = Objects.requireNonNull(dataPointFunction, "dataPointFunction");

        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.operationFactory = Objects.requireNonNull(operationFactory, "operationFactory");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }



    @Override
    public void insert(String agentId, List<T> dataPoints) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(dataPoints, "dataPoints");

        // Assert agentId
        CollectorUtils.checkAgentId(agentId);

        if (CollectionUtils.isEmpty(dataPoints)) {
            return;
        }

        dataPoints = preprocessor.apply(dataPoints);
        List<Put> puts = this.operationFactory.createPuts(agentId, agentStatType, dataPoints, this.serializer);
        if (!puts.isEmpty()) {
            TableName tableName = tableNameProvider.getTableName(this.tableName);
            this.hbaseTemplate.asyncPut(tableName, puts);
        }
    }

    protected List<T> preprocess(List<T> dataPoints) {
        return dataPoints;
    }

    @Override
    public void dispatch(AgentStatBo agentStatBo) {
        List<T> dataPoints = this.dataPointFunction.apply(agentStatBo);
        insert(agentStatBo.getAgentId(), dataPoints);
    }
}
