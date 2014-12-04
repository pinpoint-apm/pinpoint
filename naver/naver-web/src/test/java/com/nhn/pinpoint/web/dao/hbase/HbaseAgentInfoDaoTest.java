package com.nhn.pinpoint.web.dao.hbase;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;

/**
 * @author emeroad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseAgentInfoDaoTest {

	@Autowired
	private HbaseAgentInfoDao selectDao;

    @Autowired
	private com.nhn.pinpoint.collector.dao.hbase.HbaseAgentInfoDao insertDao;

	@Test
	public void testSelectAgentInfoStartTime() throws Exception {
		TAgentInfo agentInfo1 = createAgentInfo(10000);
		insertDao.insert(agentInfo1);

		TAgentInfo agentInfo2 = createAgentInfo(20000);
		insertDao.insert(agentInfo2);

		TAgentInfo agentInfo3 = createAgentInfo(30000);
		insertDao.insert(agentInfo3);

		AgentInfoBo testcaseAgent1 = selectDao.findAgentInfoBeforeStartTime("testcaseAgent", 20005);
		Assert.assertEquals(testcaseAgent1.getStartTime(), 20000);

		AgentInfoBo testcaseAgent2 = selectDao.findAgentInfoBeforeStartTime("testcaseAgent", 10004);
		Assert.assertEquals(testcaseAgent2.getStartTime(), 10000);

		AgentInfoBo testcaseAgent3 = selectDao.findAgentInfoBeforeStartTime("testcaseAgent", 50000);
		Assert.assertEquals(testcaseAgent3.getStartTime(), 30000);

	}

	private TAgentInfo createAgentInfo(long startTime) {
		TAgentInfo agentInfo = new TAgentInfo();
		agentInfo.setAgentId("testcaseAgent");
		agentInfo.setApplicationName("testcaseApplication");
		agentInfo.setHostname("testcaseHostName");
		agentInfo.setPorts("9995");
        agentInfo.setStartTimestamp(startTime);

		return agentInfo;
	}
}
