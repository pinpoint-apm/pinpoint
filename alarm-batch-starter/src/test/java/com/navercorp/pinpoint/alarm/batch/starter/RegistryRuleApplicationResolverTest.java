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
package com.navercorp.pinpoint.alarm.batch.starter;

import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.service.ServiceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What the memo is for, and what it must not do.
 *
 * <p>A sweep asks this once per rule and its rules share a handful of service names, so the
 * registry should see one query per name rather than one per rule. The failure side matters
 * more than the hit side: caching a miss would pin "no such service" onto every rule naming
 * it for the length of the window, and the registry catching up would not clear it.
 *
 * <p>Expiry is not exercised by sleeping. A zero window makes every lookup a miss, which is
 * the same branch without the wall clock.
 */
class RegistryRuleApplicationResolverTest {

    private static final Service SERVICE = new Service("svc", 42);
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    @Test
    void oneQueryAnswersEveryRuleNamingTheSameService() {
        CountingResolver registry = new CountingResolver(SERVICE);
        RegistryRuleApplicationResolver resolver = resolver(registry, ONE_MINUTE);

        Application first = resolver.resolve(ruleOn("svc"));
        Application second = resolver.resolve(ruleOn("svc"));

        assertEquals(1, registry.calls, "the second rule must not ask again");
        assertEquals(SERVICE.getServiceUid(), first.getService().getServiceUid());
        assertSame(first.getService(), second.getService());
    }

    @Test
    void aDifferentServiceIsItsOwnQuery() {
        CountingResolver registry = new CountingResolver(SERVICE);
        RegistryRuleApplicationResolver resolver = resolver(registry, ONE_MINUTE);

        resolver.resolve(ruleOn("svc"));
        resolver.resolve(ruleOn("other"));

        assertEquals(2, registry.calls);
    }

    // A registry that has not caught up yet must not be remembered as a verdict.
    @Test
    void anUnknownServiceIsAskedAboutEveryTime() {
        CountingResolver registry = new CountingResolver(null);
        RegistryRuleApplicationResolver resolver = resolver(registry, ONE_MINUTE);

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(ruleOn("gone")));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(ruleOn("gone")));

        assertEquals(2, registry.calls, "a failure must not be memoised");
    }

    @Test
    void aZeroWindowRemembersNothing() {
        CountingResolver registry = new CountingResolver(SERVICE);
        RegistryRuleApplicationResolver resolver = resolver(registry, Duration.ZERO);

        resolver.resolve(ruleOn("svc"));
        resolver.resolve(ruleOn("svc"));

        assertEquals(2, registry.calls);
    }

    private static RegistryRuleApplicationResolver resolver(ServiceModelResolver registry,
                                                            Duration memoTtl) {
        ApplicationFactory factory = Mockito.mock(ApplicationFactory.class);
        Mockito.when(factory.createApplicationByTypeName(
                        any(Service.class), anyString(), anyString()))
                .thenAnswer(call -> new Application(
                        call.getArgument(0), call.getArgument(1), ServiceType.TEST_STAND_ALONE));
        return new RegistryRuleApplicationResolver(factory, registry, memoTtl);
    }

    private static AlarmRuleV2 ruleOn(String serviceName) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setServiceName(serviceName);
        rule.setApplicationName("app");
        rule.setApplicationType("TEST_STAND_ALONE");
        return rule;
    }

    /** Answers with the given service, or raises the way the real registry does when null. */
    private static class CountingResolver extends ServiceModelResolver {
        private final Service answer;
        private int calls;

        CountingResolver(Service answer) {
            super(Mockito.mock(ServiceRegistryService.class));
            this.answer = answer;
        }

        @Override
        public Service getService(String serviceName) {
            calls++;
            if (answer == null) {
                throw new ServiceNotFoundException(serviceName);
            }
            return answer;
        }
    }
}
