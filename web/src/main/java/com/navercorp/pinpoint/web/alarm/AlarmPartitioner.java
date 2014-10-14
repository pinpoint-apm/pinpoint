package com.nhn.pinpoint.web.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.Application;

public class AlarmPartitioner implements Partitioner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int APP_COUNT = 5;
    public static final String PARTITION_NUMBER = "partition_number";
    
    @Autowired
    private ApplicationIndexDao applicationIndexDao;
    
    public AlarmPartitioner() {
    }
    
    protected AlarmPartitioner(ApplicationIndexDao applicationIndexDao) {
        this.applicationIndexDao = applicationIndexDao;
    }
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionCount = calculateGroupCount();
        Map<String, ExecutionContext> mapContext = new HashMap<String, ExecutionContext>();
        
        for (int i = 1; i <= partitionCount; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put(PARTITION_NUMBER, i);
            mapContext.put(PARTITION_NUMBER + "_" + i, executionContext);
        }
        
        return mapContext;
    }    
    
    public int calculateGroupCount() {
        List<Application> applicationList = applicationIndexDao.selectAllApplicationNames();
        int partitionCount = applicationList.size() / APP_COUNT;
        
        if (applicationList.size() % APP_COUNT != 0) {
            partitionCount++;
        }
        
        logger.info("application count is {}. patition count is {}", applicationList.size(), partitionCount);
        return partitionCount;
    }
}
