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

import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author minwoo.jung
 */
public class AlarmReader implements ItemStreamReader<Application> {

    private static final Logger logger = LogManager.getLogger(AlarmReader.class);
    private static final String CURRENT_INDEX = "current.index";

    private final ApplicationIndexDao applicationIndexDao;
    private final AlarmService alarmService;

    private final AtomicReference<List<Application>> lastApplicationsRef = new AtomicReference<>();
    private final AtomicInteger currentIndexAtom = new AtomicInteger(0);

    private List<Application> applications;

    public AlarmReader(ApplicationIndexDao applicationIndexDao, AlarmService alarmService) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
    }
    
    public Application read() {
        int currentIndex = currentIndexAtom.getAndIncrement();
        if (currentIndex < applications.size()) {
            logger.info("Reading application: {} / {}", currentIndex, applications.size());
            return applications.get(currentIndex);
        } else {
            return null;
        }
    }

    @Override
    public void open(@Nonnull ExecutionContext executionContext) throws ItemStreamException {
        logger.info("Opened alarm reader");
        this.applications = getApplications();
        logger.info("Alarm reader has {} applications", applications.size());
        if (executionContext.containsKey(CURRENT_INDEX)) {
            int loadedIndex = executionContext.getInt(CURRENT_INDEX);
            this.currentIndexAtom.set(loadedIndex);
            logger.info("Alarm reader starts from index {}", loadedIndex);
        } else {
            this.currentIndexAtom.set(0);
            logger.info("Alarm reader starts from beginning");
        }
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_INDEX, this.currentIndexAtom.get());
    }

    @Override
    public void close() throws ItemStreamException {
        logger.info("Closing alarm reader");
    }

    private List<Application> getApplications() {
        try {
            List<Application> applications = fetchApplications();
            lastApplicationsRef.set(applications);
            return applications;
        } catch (Exception e) {
            logger.error("Error occurred while fetching applications.", e);
            logger.info("Fallback to last application list.");
            List<Application> lastApplications = lastApplicationsRef.get();
            if (lastApplications == null) {
                throw new IllegalStateException("Failed to fetch applications and there is no last application list.");
            }
            return lastApplications;
        }
    }

    private List<Application> fetchApplications() {
        List<Application> applications = applicationIndexDao.selectAllApplicationNames();
        List<String> validApplicationIds = alarmService.selectApplicationId();

        List<Application> validApplications = new ArrayList<>(applications.size());
        for (Application application: applications) {
            if (validApplicationIds.contains(application.getName())) {
                validApplications.add(application);
            }
        }
        return validApplications;
    }

}
