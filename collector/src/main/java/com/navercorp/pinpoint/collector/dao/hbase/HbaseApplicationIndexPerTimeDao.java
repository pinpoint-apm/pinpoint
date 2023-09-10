package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApplicationIndexPerTimeDao;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.TimestampUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseApplicationIndexPerTimeDao implements ApplicationIndexPerTimeDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.ApplicationIndexPerTime DESCRIPTOR = HbaseColumnFamily.APPLICATION_INDEX_PER_TIME;

    private final HbaseOperations2 hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    public HbaseApplicationIndexPerTimeDao(HbaseOperations2 hbaseTemplate, TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(final AgentLifeCycleBo agentLifeCycleBo, AgentProperty agentProperty) {
        Objects.requireNonNull(agentLifeCycleBo, "agentLifeCycleBo");

        CollectorUtils.checkAgentId(agentLifeCycleBo.getAgentId());
        CollectorUtils.checkApplicationName(agentProperty.getApplicationName());

        byte[] rowKey = createRowKey(agentProperty.getApplicationName());
        final Put put = new Put(rowKey);

        final byte[] qualifier = Bytes.toBytes(agentLifeCycleBo.getAgentId());
        final byte[] value = Bytes.toBytes(agentProperty.getServiceType());
        put.addColumn(DESCRIPTOR.getName(), qualifier, value);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);

        logger.debug("Insert ApplicationIndexPerTime: {}, {}", agentLifeCycleBo, agentProperty);
    }

    byte[] createRowKey(String applicationName) {
        // rowKey = applicationName + roundedTimestamp (reversed)
        byte[] applicationNameKey = Bytes.toBytes(applicationName);
        byte[] currentRoundedTimestamp = Bytes.toBytes(TimestampUtils.reverseRoundedCurrentTimeMillis());

        byte[] rowKey = new byte[HbaseTableConstants.APPLICATION_NAME_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, applicationNameKey);
        int offset = HbaseTableConstants.APPLICATION_NAME_MAX_LEN;
        BytesUtils.writeBytes(rowKey, offset, currentRoundedTimestamp);

        return rowKey;
    }

}
