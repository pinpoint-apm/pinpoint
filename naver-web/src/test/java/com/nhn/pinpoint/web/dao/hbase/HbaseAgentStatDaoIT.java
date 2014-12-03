package com.nhn.pinpoint.web.dao.hbase;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.Range;

/**
 * @author harebox
 * @author hyungil.jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web.xml")
public class HbaseAgentStatDaoIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseAgentStatDao dao;

    @Test
    public void selectAgentStat() {
        long timestamp = System.currentTimeMillis();
        Range range = new Range(timestamp - 100000, timestamp);
        List<AgentStat> result = dao.scanAgentStatList("FRONT-WEB1", range);
        logger.debug("{}", result);
    }

}
