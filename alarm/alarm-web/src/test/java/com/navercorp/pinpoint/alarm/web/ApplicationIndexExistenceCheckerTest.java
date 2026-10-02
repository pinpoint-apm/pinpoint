/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.service.ServiceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationIndexExistenceCheckerTest {

    private static final ServiceType SPRING_BOOT =
            ServiceTypeFactory.of(1210, "SPRING_BOOT");
    private static final ServiceType TOMCAT =
            ServiceTypeFactory.of(1010, "TOMCAT");
    // Not the default service, so a hard-coded or defaulted uid fails the stub.
    private static final Service SERVICE = new Service("service", 7);

    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private ServiceModelResolver serviceModelResolver;

    private ApplicationIndexExistenceChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ApplicationIndexExistenceChecker(applicationDao, serviceModelResolver);
    }

    @Test
    void anIndexedApplicationExists() {
        when(serviceModelResolver.getService("service")).thenReturn(SERVICE);
        when(applicationDao.getApplications(eq(7), eq("app")))
                .thenReturn(List.of(new Application("app", SPRING_BOOT)));

        assertTrue(checker.exists(new AlarmApplication("service", "app", "SPRING_BOOT")));
    }

    @Test
    void theSameNameUnderAnotherTypeDoesNot() {
        // The index is keyed by both, and a rule reads the store its type names.
        when(serviceModelResolver.getService("service")).thenReturn(SERVICE);
        when(applicationDao.getApplications(eq(7), eq("app")))
                .thenReturn(List.of(new Application("app", TOMCAT)));

        assertFalse(checker.exists(new AlarmApplication("service", "app", "SPRING_BOOT")));
    }

    @Test
    void aTypeNoPluginRegisteredDoesNot() {
        when(serviceModelResolver.getService("service")).thenReturn(SERVICE);
        when(applicationDao.getApplications(eq(7), eq("app")))
                .thenReturn(List.of(new Application("app", SPRING_BOOT)));

        assertFalse(checker.exists(new AlarmApplication("service", "app", "NO_SUCH_TYPE")));
    }

    @Test
    void anUnknownServiceOwnsNothing() {
        when(serviceModelResolver.getService("gone")).thenThrow(new ServiceNotFoundException("gone"));

        assertFalse(checker.exists(new AlarmApplication("gone", "app", "SPRING_BOOT")));
        verifyNoInteractions(applicationDao);
    }

    @Test
    void anIndexedTypeNoPluginClaimsIsNotSomethingARuleMayName() {
        // Such a row reads as UNDEFINED here, and naming it back would save a rule the batch
        // raises on every cycle rather than one that reads an empty histogram.
        when(serviceModelResolver.getService("service")).thenReturn(SERVICE);
        when(applicationDao.getApplications(eq(7), eq("app")))
                .thenReturn(List.of(new Application("app", ServiceType.UNDEFINED)));

        assertFalse(checker.exists(new AlarmApplication("service", "app", "UNDEFINED")));
    }

    @Test
    void aNameTooLongForTheRowKeyIsNotIndexed() {
        // The request limit counts characters; the row key pads bytes and throws past 254.
        String longName = "\uac00".repeat(127);

        assertFalse(checker.exists(new AlarmApplication("service", longName, "SPRING_BOOT")));
        verifyNoInteractions(applicationDao);
    }
}
