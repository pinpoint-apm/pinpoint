package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_INDEX;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_INDEX_CF_AGENTS;

import com.nhn.pinpoint.collector.dao.ApplicationIndexDao;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import org.springframework.stereotype.Repository;

/**
 * application names list.
 *
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(final TAgentInfo agentInfo) {
        Put put = new Put(Bytes.toBytes(agentInfo.getApplicationName()));
        byte[] qualifier = Bytes.toBytes(agentInfo.getAgentId());
        byte[] value = Bytes.toBytes(agentInfo.getServiceType());
        
        put.add(APPLICATION_INDEX_CF_AGENTS, qualifier, value);
        
        hbaseTemplate.put(APPLICATION_INDEX, put);

        logger.debug("Insert agentInfo. {}", agentInfo);
    }
}
