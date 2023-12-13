package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
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

public class DefaultAgentStatDao<T extends AgentStatDataPoint> implements AgentStatDao<T> {

    private final AgentStatType agentStatType;
    private final HbaseTable tableName;
    private final Function<AgentStatBo, List<T>> dataPointFunction;
    private final HbasePutWriter putWriter;
    private final TableNameProvider tableNameProvider;
    private final AgentStatHbaseOperationFactory operations;
    private final AgentStatSerializer<T> serializer;

    protected Function<List<T>, List<T>> preprocessor = Function.identity();

    public DefaultAgentStatDao(AgentStatType agentStatType,
                               HbaseTable tableName,
                               Function<AgentStatBo, List<T>> dataPointFunction,
                               HbasePutWriter putWriter,
                               TableNameProvider tableNameProvider,
                               AgentStatHbaseOperationFactory operations,
                               AgentStatSerializer<T> serializer) {
        this.agentStatType = Objects.requireNonNull(agentStatType, "agentStatType");
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.dataPointFunction = Objects.requireNonNull(dataPointFunction, "dataPointFunction");

        this.putWriter = Objects.requireNonNull(putWriter, "putWriter");

        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.operations = Objects.requireNonNull(operations, "operations");
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
        List<Put> puts = this.operations.createPuts(agentId, agentStatType, dataPoints, this.serializer);
        if (puts.isEmpty()) {
            return;
        }
        TableName tableName = tableNameProvider.getTableName(this.tableName);
        this.putWriter.put(tableName, puts);
    }

    @Override
    public void dispatch(AgentStatBo agentStatBo) {
        Objects.requireNonNull(agentStatBo, "agentStatBo");

        List<T> dataPoints = this.dataPointFunction.apply(agentStatBo);
        insert(agentStatBo.getAgentId(), dataPoints);
    }
}
