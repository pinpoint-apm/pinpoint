package com.nhn.pinpoint.web.dao.hbase;

import java.util.List;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.web.vo.Range;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author harebox
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class HbaseAgentStatDaoTest {

	@Autowired
	private HbaseAgentStatDao dao;
	
	@Test
	public void selectAgentStat() {
		long timestamp = System.currentTimeMillis();
        Range range = new Range(timestamp - 100000, timestamp);
        List<TAgentStat> result = dao.scanAgentStatList("FRONT-WEB1", range);
		System.out.println(result);
	}
	
}
