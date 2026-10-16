/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author youngjin.kim2
 */
public class AgentCountReader implements ItemReader<Application>, StepExecutionListener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final BatchApplicationIndexService batchApplicationIndexService;
    private final ServiceRegistryService serviceRegistryService;
    private final boolean allServicesEnable;
    private final int serviceListLimit;

    private Queue<Application> applicationQueue;

    public AgentCountReader(BatchApplicationIndexService batchApplicationIndexService,
                            ServiceRegistryService serviceRegistryService,
                            boolean allServicesEnable,
                            int serviceListLimit) {
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "batchApplicationIndexService");
        this.serviceRegistryService = Objects.requireNonNull(serviceRegistryService, "serviceRegistryService");
        this.allServicesEnable = allServicesEnable;
        Assert.isTrue(serviceListLimit > 0, "serviceListLimit must be positive");
        this.serviceListLimit = serviceListLimit;
    }

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        // DEFAULT is well-known and not a row in the service table
        this.applicationQueue = new ConcurrentLinkedQueue<>(batchApplicationIndexService.selectApplications(Service.DEFAULT));

        if (!allServicesEnable) {
            return;
        }
        for (Service service : getServices()) {
            if (ServiceUid.isReservedUid(service.getServiceUid())) {
                continue;
            }
            this.applicationQueue.addAll(batchApplicationIndexService.selectApplications(service));
        }
    }

    private List<Service> getServices() {
        // fetch one extra row to detect truncation
        List<Service> services = serviceRegistryService.getServiceList(serviceListLimit + 1);
        if (services.size() > serviceListLimit) {
            // fail the step instead of persisting a partial total
            throw new IllegalStateException("Service count exceeds limit " + serviceListLimit);
        }
        logger.info("Service count {} (limit {})", services.size(), serviceListLimit);
        return services;
    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        return null;
    }

    @Override
    public Application read() {
        return applicationQueue.poll();
    }

}
