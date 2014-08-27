package com.nhn.pinpoint.collector.mapper.thrift;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;

/**
 * @author hyungil.jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class AgentStatCpuLoadBoMapperTest {

    // CPU 사용량 소수점 2자리 표시
    private static final double DELTA = 1e-4;

    @Autowired
    private AgentStatCpuLoadBoMapper mapper;

    @Test
    public void testValidMap() {
        // Given
        final TAgentStat thriftObj = new TAgentStat();
        thriftObj.setAgentId("agentId");
        thriftObj.setStartTimestamp(0L);
        thriftObj.setTimestamp(1L);
        final TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(99.0D);
        cpuLoad.setSystemCpuLoad(99.9D);
        thriftObj.setCpuLoad(cpuLoad);
        // When
        AgentStatCpuLoadBo mappedBo = mapper.map(thriftObj);
        // Then
        assertEquals(mappedBo.getAgentId(), thriftObj.getAgentId());
        assertEquals(mappedBo.getStartTimestamp(), thriftObj.getStartTimestamp());
        assertEquals(mappedBo.getTimestamp(), thriftObj.getTimestamp());
        assertEquals(mappedBo.getJvmCpuLoad(), cpuLoad.getJvmCpuLoad(), DELTA);
        assertEquals(mappedBo.getSystemCpuLoad(), cpuLoad.getSystemCpuLoad(), DELTA);
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
    public void mapShouldNotThrowExceptionForNullCpuLoad() {
        // Given
        final TAgentStat thriftObj = new TAgentStat();
        thriftObj.setAgentId("agentId");
        thriftObj.setStartTimestamp(0L);
        thriftObj.setTimestamp(1L);
        thriftObj.setCpuLoad(null);
        // When
        AgentStatCpuLoadBo mappedBo = mapper.map(thriftObj);
        // Then
        assertEquals(mappedBo.getAgentId(), thriftObj.getAgentId());
        assertEquals(mappedBo.getStartTimestamp(), thriftObj.getStartTimestamp());
        assertEquals(mappedBo.getTimestamp(), thriftObj.getTimestamp());
        assertTrue(mappedBo.getJvmCpuLoad() < 0.0D);
        assertTrue(mappedBo.getSystemCpuLoad() < 0.0D);
    }

    @Test
    public void mapShouldNotThrowExceptionForNullJvmCpuLoad() {
        // Given
        final TAgentStat thriftObj = new TAgentStat();
        thriftObj.setAgentId("agentId");
        thriftObj.setStartTimestamp(0L);
        thriftObj.setTimestamp(1L);
        final TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoadIsSet(false);
        cpuLoad.setSystemCpuLoad(99.0D);
        thriftObj.setCpuLoad(cpuLoad);
        // When
        AgentStatCpuLoadBo mappedBo = mapper.map(thriftObj);
        // Then
        assertEquals(mappedBo.getAgentId(), thriftObj.getAgentId());
        assertEquals(mappedBo.getStartTimestamp(), thriftObj.getStartTimestamp());
        assertEquals(mappedBo.getTimestamp(), thriftObj.getTimestamp());
        assertTrue(mappedBo.getJvmCpuLoad() < 0.0D);
        assertEquals(mappedBo.getSystemCpuLoad(), cpuLoad.getSystemCpuLoad(), DELTA);
    }

    @Test
    public void mapShouldNotThrowExceptionForNullSystemCpuLoad() {
        // Given
        final TAgentStat thriftObj = new TAgentStat();
        thriftObj.setAgentId("agentId");
        thriftObj.setStartTimestamp(0L);
        thriftObj.setTimestamp(1L);
        final TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(99.0D);
        cpuLoad.setSystemCpuLoadIsSet(false);
        thriftObj.setCpuLoad(cpuLoad);
        // When
        AgentStatCpuLoadBo mappedBo = mapper.map(thriftObj);
        // Then
        assertEquals(mappedBo.getAgentId(), thriftObj.getAgentId());
        assertEquals(mappedBo.getStartTimestamp(), thriftObj.getStartTimestamp());
        assertEquals(mappedBo.getTimestamp(), thriftObj.getTimestamp());
        assertEquals(mappedBo.getJvmCpuLoad(), cpuLoad.getJvmCpuLoad(), DELTA);
        assertTrue(mappedBo.getSystemCpuLoad() < 0.0D);
    }
}
