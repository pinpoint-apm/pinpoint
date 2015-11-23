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

package com.navercorp.pinpoint.web.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author minwoo.jung
 */
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
        Map<String, ExecutionContext> mapContext = new HashMap<>();

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

        logger.info("application count is {}. partition count is {}", applicationList.size(), partitionCount);
        return partitionCount;
    }
}
