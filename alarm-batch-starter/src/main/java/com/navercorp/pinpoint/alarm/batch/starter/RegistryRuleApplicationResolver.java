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

import com.navercorp.pinpoint.alarm.core.service.RuleApplicationResolver;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.service.ServiceNotFoundException;

import java.util.Objects;

/**
 * Resolves a rule's application against the registry this installation keeps. Here rather
 * than beside the metric query services, because the registry is a store and the module
 * that reads numbers does not depend on the one that knows what a service name means.
 *
 * <p>A service name the registry does not know is raised rather than read as the default
 * service, which would query an application belonging to someone else and report what it
 * found.
 */
public class RegistryRuleApplicationResolver implements RuleApplicationResolver {

    private final ApplicationFactory applicationFactory;
    private final ServiceModelResolver serviceModelResolver;

    public RegistryRuleApplicationResolver(ApplicationFactory applicationFactory,
                                           ServiceModelResolver serviceModelResolver) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.serviceModelResolver =
                Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
    }

    @Override
    public Application resolve(AlarmRuleV2 rule) {
        Application application = applicationFactory.createApplicationByTypeName(
                lookUp(rule), rule.getApplicationName(), rule.getApplicationType());
        if (ServiceType.UNDEFINED.equals(application.getServiceType())) {
            // Raised rather than logged: every store this reads is keyed by the type, so
            // reading them under the undefined one answers an empty histogram and no agents,
            // which a condition cannot tell apart from a healthy application.
            throw new IllegalStateException("No loaded plugin claims application type '"
                    + rule.getApplicationType() + "', rule_id=" + rule.getId());
        }
        return application;
    }

    /**
     * Rewrapped so the message names the rule. {@link ServiceNotFoundException} carries only the
     * service name, and this is the one place that knows which rule asked for it; the original
     * stays as the cause, so the CHECK_FAILED row still records what actually failed.
     */
    private Service lookUp(AlarmRuleV2 rule) {
        try {
            return serviceModelResolver.getService(rule.getServiceName());
        } catch (ServiceNotFoundException e) {
            throw new IllegalArgumentException("No such service '" + rule.getServiceName()
                    + "', rule_id=" + rule.getId(), e);
        }
    }
}
