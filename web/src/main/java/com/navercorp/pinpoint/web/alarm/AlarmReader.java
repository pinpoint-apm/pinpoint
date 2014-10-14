package com.nhn.pinpoint.web.alarm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.checker.AlarmChecker;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.AlarmResourceDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.Application;

public class AlarmReader implements ItemReader<AlarmChecker>, StepExecutionListener {
    
    @Autowired
    private DataCollectorFactory dataCollectorFactory;
    
    @Autowired
    private ApplicationIndexDao applicationIndexDao;
    
    @Autowired
    private AlarmResourceDao alarmResourceDao;
    
    private final Queue<AlarmChecker> checkers = new LinkedList<AlarmChecker>();

    public AlarmReader() {
    }
    
    protected AlarmReader(DataCollectorFactory dataCollectorFactory, ApplicationIndexDao applicationIndexDao, AlarmResourceDao alarmResourceDao) {
        this.dataCollectorFactory = dataCollectorFactory;
        this.applicationIndexDao = applicationIndexDao;
        this.alarmResourceDao = alarmResourceDao;
    }
    
    public AlarmChecker read() {
        return checkers.poll();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        List<Application> applicationList = applicationIndexDao.selectAllApplicationNames();
        int appSize = applicationList.size();
        int partitionNumber = (Integer) stepExecution.getExecutionContext().get(AlarmPartitioner.PARTITION_NUMBER);
        int from = (partitionNumber - 1) * AlarmPartitioner.APP_COUNT;
        int to = partitionNumber * AlarmPartitioner.APP_COUNT;
        
        if (appSize < from) {
            return;
        }
        if (appSize < to) {
            to = appSize;
        }

        
        for(int i = from; i < to; i++) {
            addChecker(applicationList.get(i));
        }
    }

    private void addChecker(Application application) {
        List<Rule> rules = alarmResourceDao.selectAppRule(application.getName());
        long timeSlotEndTime = System.currentTimeMillis();
        Map<DataCollectorCategory, DataCollector> collectorMap = new HashMap<DataCollectorCategory, DataCollector>();
        
        for (Rule rule : rules) {
            CheckerCategory checkerCategory = CheckerCategory.getValue(rule.getCheckerName());
            DataCollector collector = collectorMap.get(checkerCategory);
            
            if(collector == null) {
                collector = dataCollectorFactory.createDataCollector(checkerCategory, application, timeSlotEndTime);
                collectorMap.put(collector.getDataCollectorCategory(), collector);
            }
            
            AlarmChecker checker = checkerCategory.createChecker(collector, rule);
            checkers.add(checker);
        }
        
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
