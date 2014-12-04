package com.nhn.pinpoint.collector.mapper.thrift;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author hyungil.jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class AgentStatMemoryGcBoMapperTest {

    @Autowired
    private AgentStatMemoryGcBoMapper mapper;

    @Test
    public void testValidMap() {
        // Given
        final TAgentStat thriftObj = new TAgentStat();
        thriftObj.setAgentId("agentId");
        thriftObj.setStartTimestamp(0L);
        thriftObj.setTimestamp(1L);
        final TJvmGc gc = new TJvmGc();
        gc.setType(TJvmGcType.UNKNOWN);
        gc.setJvmMemoryHeapUsed(999L);
        gc.setJvmMemoryHeapMax(1000L);
        gc.setJvmMemoryNonHeapUsed(1999L);
        gc.setJvmMemoryNonHeapMax(2000L);
        gc.setJvmGcOldCount(3L);
        gc.setJvmGcOldTime(10L);
        thriftObj.setGc(gc);
        // When
        AgentStatMemoryGcBo mappedBo = mapper.map(thriftObj);
        // Then
        assertEquals(mappedBo.getAgentId(), thriftObj.getAgentId());
        assertEquals(mappedBo.getStartTimestamp(), thriftObj.getStartTimestamp());
        assertEquals(mappedBo.getTimestamp(), thriftObj.getTimestamp());
        assertEquals(mappedBo.getGcType(), gc.getType().name());
        assertEquals(mappedBo.getJvmMemoryHeapUsed(), gc.getJvmMemoryHeapUsed());
        assertEquals(mappedBo.getJvmMemoryHeapMax(), gc.getJvmMemoryHeapMax());
        assertEquals(mappedBo.getJvmMemoryNonHeapUsed(), gc.getJvmMemoryNonHeapUsed());
        assertEquals(mappedBo.getJvmMemoryNonHeapMax(), gc.getJvmMemoryNonHeapMax());
        assertEquals(mappedBo.getJvmGcOldCount(), gc.getJvmGcOldCount());
        assertEquals(mappedBo.getJvmGcOldTime(), gc.getJvmGcOldTime());
    }

    @Test(expected=NullPointerException.class)
    public void mapShouldThrowNpeForNullThriftObject() {
        // Given
        final TAgentStat thriftObj = null;
        // When
        mapper.map(thriftObj);
        // Then
        fail();
    }

    @Test
    public void mapShouldNotThrowExceptionForNullGc() {
        // Given
        final TAgentStat thriftObj = new TAgentStat();
        thriftObj.setAgentId("agentId");
        thriftObj.setStartTimestamp(0L);
        thriftObj.setTimestamp(1L);
        thriftObj.setGc(null);
        // When
        AgentStatMemoryGcBo mappedBo = mapper.map(thriftObj);
        // Then
        assertEquals(mappedBo.getAgentId(), thriftObj.getAgentId());
        assertEquals(mappedBo.getStartTimestamp(), thriftObj.getStartTimestamp());
        assertEquals(mappedBo.getTimestamp(), thriftObj.getTimestamp());
        assertEquals(mappedBo.getGcType(), TJvmGcType.UNKNOWN.name());
        assertEquals(mappedBo.getJvmMemoryHeapUsed(), 0L);
        assertEquals(mappedBo.getJvmMemoryHeapMax(), 0L);
        assertEquals(mappedBo.getJvmMemoryNonHeapUsed(), 0L);
        assertEquals(mappedBo.getJvmMemoryNonHeapMax(), 0L);
        assertEquals(mappedBo.getJvmGcOldCount(), 0L);
        assertEquals(mappedBo.getJvmGcOldTime(), 0L);
    }
}
