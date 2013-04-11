package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.APPLICATION_INDEX;
import static com.profiler.common.hbase.HBaseTables.APPLICATION_INDEX_CF_AGENTS;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto2.thrift.AgentInfo;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.server.dao.ApplicationIndexDao;

/**
 * application names list.
 *
 * @author netspider
 */
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(final AgentInfo agentInfo) {
        Put put = new Put(Bytes.toBytes(agentInfo.getApplicationName()), agentInfo.getTimestamp());
        byte[] agentId = Bytes.toBytes(agentInfo.getAgentId());
        byte[] serviceType = Bytes.toBytes(agentInfo.getServiceType());
        
        put.add(APPLICATION_INDEX_CF_AGENTS, agentId, serviceType);
        
        hbaseTemplate.put(APPLICATION_INDEX, put);

        logger.debug("Insert agentInfo. {}", agentInfo);
    }
}
