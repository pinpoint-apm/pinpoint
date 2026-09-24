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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves a rule's application against the registry this installation keeps. Here rather
 * than beside the metric query services, because the registry is a store and the module
 * that reads numbers does not depend on the one that knows what a service name means.
 *
 * <p>The lookup is memoised for a short window: every rule resolves before anything is read
 * and a sweep's rules share a handful of service names, so without it the same row is
 * fetched once per rule out of the pool the sweep also writes through. A window rather than
 * the sweep itself, because the job that runs the sweep routes by port and never names an
 * implementation, so it has nothing to clear. Failures are not memoised.
 *
 * <p>A service name the registry does not know is raised rather than read as the default
 * service, which would query an application belonging to someone else and report what it
 * found.
 */
public class RegistryRuleApplicationResolver implements RuleApplicationResolver {

    private static final Logger logger = LogManager.getLogger(RegistryRuleApplicationResolver.class);

    private final ApplicationFactory applicationFactory;
    private final ServiceModelResolver serviceModelResolver;
    private final long memoTtlMillis;  // how long an answer stays usable
    private final Map<String, Memo> memo = new ConcurrentHashMap<>();

    public RegistryRuleApplicationResolver(ApplicationFactory applicationFactory,
                                           ServiceModelResolver serviceModelResolver,
                                           Duration memoTtl) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.serviceModelResolver =
                Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
        this.memoTtlMillis = Objects.requireNonNull(memoTtl, "memoTtl").toMillis();
    }

    @Override
    public Application resolve(AlarmRuleV2 rule) {
        Application application = applicationFactory.createApplicationByTypeName(
                serviceOf(rule), rule.getApplicationName(), rule.getApplicationType());
        if (ServiceType.UNDEFINED.equals(application.getServiceType())) {
            logger.warn("No loaded plugin claims application type '{}', rule_id={}",
                    rule.getApplicationType(), rule.getId());
        }
        return application;
    }

    private Service serviceOf(AlarmRuleV2 rule) {
        String serviceName = rule.getServiceName();

        Memo cached = memo.get(serviceName);
        if (cached != null && cached.expiresAtMillis() > System.currentTimeMillis()) {
            return cached.service();
        }

        Service service = lookUp(rule);
        // Dated from when the answer arrived, not from when it was asked for. A registry
        // slower than the window would otherwise store an entry that is already expired --
        // exactly in the stretch where not asking again is worth the most.
        memo.put(serviceName, new Memo(service, System.currentTimeMillis() + memoTtlMillis));
        return service;
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

    private record Memo(Service service, long expiresAtMillis) {
    }
}
