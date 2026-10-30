/*
 * Copyright 2026 NAVER Corp.
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
 *
 */
package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.dao.dto.ServiceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The alarm listener depends, through several beans, on {@link ServiceRegistryService}
 * itself. A plain constructor-injected list fails the web context with a creation cycle.
 */
class ServiceDeletionListenerContextTest {

    @Test
    void listenerThatDependsOnTheRegistryDoesNotFailTheContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Beans.class)) {
            ServiceRegistryDao dao = context.getBean(ServiceRegistryDao.class);
            ServiceEntity entity = new ServiceEntity();
            entity.setUid(7);
            entity.setName("svc");
            when(dao.selectServiceByNameForUpdate("svc")).thenReturn(entity);

            context.getBean(ServiceRegistryService.class).deleteService("svc");

            assertThat(context.getBean(Calls.class).listeners)
                    .containsExactly("first", "registryDependent");
        }
    }

    @Configuration
    @Import({ServiceRegistryServiceImpl.class, Calls.class, RegistryDependentListener.class, FirstListener.class})
    static class Beans {
        @Bean
        ServiceRegistryDao serviceRegistryDao() {
            return mock(ServiceRegistryDao.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        IdGenerator<ServiceUid> serviceUidGenerator() {
            return mock(IdGenerator.class);
        }
    }

    static class Calls {
        final List<String> listeners = new ArrayList<>();
    }

    static class RegistryDependentListener implements ServiceDeletionListener {
        private final Calls calls;

        RegistryDependentListener(ServiceRegistryService serviceRegistryService, Calls calls) {
            this.calls = calls;
        }

        @Override
        public void onServiceDelete(Service service) {
            calls.listeners.add("registryDependent");
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    static class FirstListener implements ServiceDeletionListener {
        private final Calls calls;

        FirstListener(Calls calls) {
            this.calls = calls;
        }

        @Override
        public void onServiceDelete(Service service) {
            calls.listeners.add("first");
        }
    }
}
