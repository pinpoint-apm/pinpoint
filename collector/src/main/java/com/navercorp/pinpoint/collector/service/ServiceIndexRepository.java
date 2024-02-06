/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ServiceIndexDao;
import com.navercorp.pinpoint.collector.vo.ApplicationIndex;
import com.navercorp.pinpoint.collector.vo.ServiceHasApplication;
import com.navercorp.pinpoint.collector.vo.ServiceIndex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
@Service
public class ServiceIndexRepository implements InitializingBean {

    private final ServiceIndexDao serviceIndexDao;

    private final AtomicBoolean concurrentFence = new AtomicBoolean(false);
    private final AtomicReference<State> stateRef = new AtomicReference<>(State.EMPTY);

    public ServiceIndexRepository(ServiceIndexDao serviceIndexDao) {
        this.serviceIndexDao = Objects.requireNonNull(serviceIndexDao, "serviceIndexDao");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void reload() {
        try {
            if (concurrentFence.compareAndSet(false, true)) {
                reload0();
            }
        } finally {
            concurrentFence.set(false);
        }
    }

    private void reload0() {
        List<ServiceIndex> serviceIndices = serviceIndexDao.selectAllServices();
        List<ApplicationIndex> applicationIndices = serviceIndexDao.selectAllApplications();
        List<ServiceHasApplication> serviceHasApplications = serviceIndexDao.selectAllServiceHasApplications();

        State newState = State.build(serviceIndices, applicationIndices, serviceHasApplications);
        stateRef.set(newState);
    }

    public Long getServiceIdByName(String name) {
        return stateRef.get().getServiceIdByName(name);
    }

    public Long getApplicationId(Long serviceId, String applicationName) {
        return stateRef.get().getApplicationId(serviceId, applicationName);
    }

    private record State(
            TreeSet<ServiceIndex> servicesByNames,
            TreeSet<ApplicationIndex> applicationsByIds,
            TreeSet<ServiceHasApplication> serviceHasApplications
    ) {

        static final State EMPTY = new State(
                new TreeSet<>(Comparator.comparing(ServiceIndex::name)),
                new TreeSet<>(Comparator.comparingLong(ApplicationIndex::id)),
                new TreeSet<>(Comparator.comparingLong(ServiceHasApplication::serviceId))
        );

        static State build(
                List<ServiceIndex> serviceIndices,
                List<ApplicationIndex> applicationIndices,
                List<ServiceHasApplication> serviceHasApplications
        ) {
            TreeSet<ServiceIndex> servicesByNames = new TreeSet<>(Comparator.comparing(ServiceIndex::name));
            servicesByNames.addAll(serviceIndices);

            TreeSet<ApplicationIndex> applicationsByIds = new TreeSet<>(Comparator.comparingLong(ApplicationIndex::id));
            applicationsByIds.addAll(applicationIndices);

            TreeSet<ServiceHasApplication> serviceHasApplicationsSet =
                    new TreeSet<>(Comparator.comparingLong(ServiceHasApplication::serviceId));
            serviceHasApplicationsSet.addAll(serviceHasApplications);

            return new State(servicesByNames, applicationsByIds, serviceHasApplicationsSet);
        }

        Long getServiceIdByName(String name) {
            ServiceIndex ceil = servicesByNames.ceiling(new ServiceIndex(null, name));
            if (ceil == null) {
                return null;
            }
            if (ceil.name().equals(name)) {
                return ceil.id();
            }
            return null;
        }

        String getApplicationName(Long applicationId) {
            ApplicationIndex applicationIndex = applicationsByIds.ceiling(new ApplicationIndex(applicationId, null));
            if (applicationIndex == null) {
                return null;
            }
            if (applicationIndex.id().equals(applicationId)) {
                return applicationIndex.name();
            }
            return null;
        }

        Long getApplicationId(Long serviceId, String applicationName) {
            SortedSet<ServiceHasApplication> serviceHasApplications =
                    this.serviceHasApplications.tailSet(new ServiceHasApplication(serviceId, 0L));
            for (ServiceHasApplication serviceHasApplication : serviceHasApplications) {
                if (!Objects.equals(serviceHasApplication.serviceId(), serviceId)) {
                    break;
                }
                if (applicationName.equals(getApplicationName(serviceHasApplication.applicationId()))) {
                    return serviceHasApplication.applicationId();
                }
            }
            return null;
        }

    }

}
