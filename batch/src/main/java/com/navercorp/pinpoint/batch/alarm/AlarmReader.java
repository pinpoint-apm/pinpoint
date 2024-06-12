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

import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.ApplicationService;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nonnull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author minwoo.jung
 */
public class AlarmReader implements ItemReader<Application>, StepExecutionListener {

    private final ApplicationService applicationService;
    private final AlarmService alarmService;

    private Queue<Application> applicationQueue;

    public AlarmReader(ApplicationService applicationService, AlarmService alarmService) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
    }
    
    public Application read() {
        return applicationQueue.poll();
    }

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        this.applicationQueue = new ConcurrentLinkedQueue<>(fetchApplications());
    }

    private List<Application> fetchApplications() {
        List<Application> applications = this.applicationService.getApplications();
        List<String> validApplicationIds = alarmService.selectApplicationId();

        List<Application> validApplications = new ArrayList<>(applications.size());
        for (Application application: applications) {
            if (validApplicationIds.contains(application.name())) {
                validApplications.add(application);
            }
        }
        return validApplications;
    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        return null;
    }
}
