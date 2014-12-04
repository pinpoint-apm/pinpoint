package com.nhn.pinpoint.collector.dao.hbase;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.hadoop.hbase.client.Put;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.collector.dao.AgentStatDao;
import com.nhn.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author hyungil.jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseAgentStatDaoTest {

    @Mock
    private HbaseOperations2 hbaseTemplate;

    @Spy
    @Autowired
    @Qualifier("agentStatMemoryGcBoMapper")
    private ThriftBoMapper<AgentStatMemoryGcBo, TAgentStat> agentStatMemoryGcBoMapper;

    @Spy
    @Autowired
    @Qualifier("agentStatCpuLoadBoMapper")
    private ThriftBoMapper<AgentStatCpuLoadBo, TAgentStat> agentStatCpuLoadBoMapper;

    @Spy
    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

    @InjectMocks
    private AgentStatDao agentStatDao = new HbaseAgentStatDao();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInsert() {
        // Given
        final String agentId = "agentId";
        final long startTimestamp = Long.MAX_VALUE;
        final TAgentStat agentStat = createAgentStat(agentId, startTimestamp, createTJvmGc(agentId, startTimestamp), createTCpuLoad());
        // When
        agentStatDao.insert(agentStat);
        // Then
        verify(hbaseTemplate).put(eq(HBaseTables.AGENT_STAT), isA(Put.class));
    }

    private TAgentStat createAgentStat(String agentId, long startTimestamp, TJvmGc gc, TCpuLoad cpuLoad) {
        final TAgentStat agentStat = new TAgentStat();
        agentStat.setAgentId(agentId);
        agentStat.setStartTimestamp(startTimestamp);
        agentStat.setGc(gc);
        agentStat.setCpuLoad(cpuLoad);
        return agentStat;
    }

    private TJvmGc createTJvmGc(String agentId, long startTimestamp) {
        final TJvmGc jvmGc = new TJvmGc();
        jvmGc.setType(TJvmGcType.G1);
        jvmGc.setJvmMemoryHeapUsed(Long.MIN_VALUE);
        jvmGc.setJvmMemoryHeapMax(Long.MAX_VALUE);
        jvmGc.setJvmMemoryNonHeapUsed(Long.MIN_VALUE);
        jvmGc.setJvmMemoryNonHeapMax(Long.MAX_VALUE);
        jvmGc.setJvmGcOldCount(1L);
        jvmGc.setJvmGcOldTime(1L);
        return jvmGc;
    }

    private TCpuLoad createTCpuLoad() {
        final TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(Double.MIN_VALUE);
        cpuLoad.setSystemCpuLoad(Double.MAX_VALUE);
        return cpuLoad;
    }

}
