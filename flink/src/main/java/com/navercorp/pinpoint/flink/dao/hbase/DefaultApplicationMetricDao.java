package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.ApplicationStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DefaultApplicationMetricDao<T extends JoinStatBo> implements ApplicationMetricDao<T> {
    protected final Logger logger = LogManager.getLogger(this.getClass());

    private final StatType statType;
    private final Function<JoinApplicationStatBo, List<T>> appStatFunction;
    private final ApplicationStatSerializer<T> serializer;

    private final HbaseTable tableName;
    private final HbaseTemplate2 hbaseTemplate2;
    private final ApplicationStatHbaseOperationFactory operations;
    private final TableNameProvider tableNameProvider;

    public DefaultApplicationMetricDao(StatType statType,
                                       Function<JoinApplicationStatBo, List<T>> appStatFunction,
                                       ApplicationStatSerializer<T> serializer,

                                       HbaseTable tableName,
                                       HbaseTemplate2 hbaseTemplate2,
                                       ApplicationStatHbaseOperationFactory operations,
                                       TableNameProvider tableNameProvider) {
        this.statType = Objects.requireNonNull(statType, "statType");
        this.appStatFunction = Objects.requireNonNull(appStatFunction, "dataPointFunction");
        this.serializer = Objects.requireNonNull(serializer, "activeTraceSerializer");

        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.hbaseTemplate2 = Objects.requireNonNull(hbaseTemplate2, "hbaseTemplate2");
        this.operations = Objects.requireNonNull(operations, "operations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public void insert(String id, long timestamp, List<T> appStatBoList) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(appStatBoList, "appStatBoList");

        if (logger.isDebugEnabled()) {
            logger.debug("[insert] {} : ({})", DateTimeFormatUtils.format(timestamp), appStatBoList);
        }
        List<Put> activeTracePuts = operations.createPuts(id, appStatBoList, statType, serializer);
        if (!activeTracePuts.isEmpty()) {
            TableName applicationStatAggreTableName = tableNameProvider.getTableName(tableName);
            hbaseTemplate2.asyncPut(applicationStatAggreTableName, activeTracePuts);
        }
    }

    @Override
    public void insert(JoinApplicationStatBo applicationStatBo) {
        Objects.requireNonNull(applicationStatBo, "applicationStatBo");

        List<T> statBo = appStatFunction.apply(applicationStatBo);
        insert(applicationStatBo.getId(), applicationStatBo.getTimestamp(), statBo);
    }
}
