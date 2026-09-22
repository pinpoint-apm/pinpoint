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


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryRuleApplicationResolverTest {

    private static final Service SERVICE = new Service("svc", 42);

    @Test
    void theRuleServiceIsResolvedOntoTheApplication() {
        RegistryRuleApplicationResolver resolver = resolver(new FixedResolver(SERVICE));

        Application application = resolver.resolve(ruleOn("svc"));

        assertEquals(SERVICE.getServiceUid(), application.getService().getServiceUid());
    }

    @Test
    void anUnknownServiceIsRaisedNamingTheRule() {
        RegistryRuleApplicationResolver resolver = resolver(new FixedResolver(null));

        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> resolver.resolve(ruleOn("gone")));

        assertTrue(e.getMessage().contains("rule_id=1"), e.getMessage());
    }

    private static RegistryRuleApplicationResolver resolver(ServiceModelResolver registry) {
        ApplicationFactory factory = Mockito.mock(ApplicationFactory.class);
        Mockito.when(factory.createApplicationByTypeName(
                        any(Service.class), anyString(), anyString()))
                .thenAnswer(call -> new Application(
                        call.getArgument(0), call.getArgument(1), ServiceType.TEST_STAND_ALONE));
        return new RegistryRuleApplicationResolver(factory, registry);
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
    private static class FixedResolver extends ServiceModelResolver {
        private final Service answer;

        FixedResolver(Service answer) {
            super(Mockito.mock(ServiceRegistryService.class));
            this.answer = answer;
        }

        @Override
        public Service getService(String serviceName) {
            if (answer == null) {
                throw new ServiceNotFoundException(serviceName);
            }
            return answer;
        }
    }
}
