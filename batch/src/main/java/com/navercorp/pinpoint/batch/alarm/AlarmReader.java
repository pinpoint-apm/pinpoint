/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author minwoo.jung
 */
public class AlarmReader implements ItemReader<AlarmChecker>, StepExecutionListener {
    
    private final DataCollectorFactory dataCollectorFactory;
    
    private final ApplicationIndexDao applicationIndexDao;
    
    private final AlarmService alarmService;
    
    private final Queue<AlarmChecker> checkers = new ConcurrentLinkedDeque<>();

    public AlarmReader(DataCollectorFactory dataCollectorFactory, ApplicationIndexDao applicationIndexDao, AlarmService alarmService) {
        this.dataCollectorFactory = Objects.requireNonNull(dataCollectorFactory, "dataCollectorFactory");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
    }
    
    public AlarmChecker read() {
        return checkers.poll();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        List<Application> applicationList = applicationIndexDao.selectAllApplicationNames();

        for (Application application : applicationList) {
            addChecker(application);
        }
    }

    private void addChecker(Application application) {
        List<Rule> rules = alarmService.selectRuleByApplicationId(application.getName());
        long timeSlotEndTime = System.currentTimeMillis();
        Map<DataCollectorCategory, DataCollector> collectorMap = new HashMap<>();
        
        for (Rule rule : rules) {
            CheckerCategory checkerCategory = CheckerCategory.getValue(rule.getCheckerName());
            DataCollector collector = collectorMap.get(checkerCategory.getDataCollectorCategory());
            if (collector == null) {
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
