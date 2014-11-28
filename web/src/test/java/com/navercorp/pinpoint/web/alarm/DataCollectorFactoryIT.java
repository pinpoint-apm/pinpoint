package com.nhn.pinpoint.web.alarm;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
@Ignore
public class DataCollectorFactoryIT {

    @Autowired
    private DataCollectorFactory factory;
    
    @Test
    public void createDataCollector() {
        DataCollector collector = factory.createDataCollector(CheckerCategory.SLOW_COUNT, null, 0);
        assertNotNull(collector);
    }

}
