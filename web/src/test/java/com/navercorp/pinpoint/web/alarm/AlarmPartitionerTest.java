package com.nhn.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.Application;

public class AlarmPartitionerTest {

    private static ApplicationIndexDao dao;
    
    @Test
    public void partitionTest() {
        AlarmPartitioner partitioner = new AlarmPartitioner(dao);
        Map<String, ExecutionContext> partitions = partitioner.partition(0);
        Assert.assertEquals(8, partitions.size());
    }
    
    @BeforeClass
    public static void beforeClass() {
        dao = new ApplicationIndexDao() {
            
            @Override
            public List<Application> selectAllApplicationNames() {
                List<Application> apps = new LinkedList<Application>();
                
                for(int i = 0; i <= 37; i++) {
                    apps.add(new Application("app" + i, ServiceType.TOMCAT));
                }
                
                return apps;
            }
            
            @Override
            public List<String> selectAgentIds(String applicationName) {
                return null;
            }
            
            @Override
            public void deleteApplicationName(String applicationName) {
            }
        };
    }

}
