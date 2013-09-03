package com.nhn.pinpoint.web.dao.hbase;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.thrift.dto.AgentStat;

/**
 * @author harebox
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseAgentStatDaoTest {

	@Autowired
	private HbaseAgentStatDao dao;
	
	@Test
	public void selectAgentStat() {
		long timestamp = System.currentTimeMillis();
		List<AgentStat> result = dao.scanAgentStatList("FRONT-WEB", timestamp - 100000, timestamp);
		System.out.println(result);
	}
	
}
