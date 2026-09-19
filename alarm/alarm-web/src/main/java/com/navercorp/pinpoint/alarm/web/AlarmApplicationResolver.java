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

import com.navercorp.pinpoint.alarm.service.AlarmApplicationExistenceChecker;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turns "does the application a rule targets exist?" into an HTTP answer.
 *
 * <p>Which registry owns the application depends on its type, so the lookup itself
 * is delegated to the {@link AlarmApplicationExistenceChecker} that claims the
 * type. An unclaimed type is a bad request rather than a missing application:
 * nothing can say whether it exists.
 */
@Component
public class AlarmApplicationResolver {

    private final Map<String, AlarmApplicationExistenceChecker> byType;

    /**
     * @throws IllegalStateException when two checkers claim one type -- bean order would
     *         otherwise decide which registry a target is validated against, accepting or
     *         rejecting it for the wrong reason.
     */
    public AlarmApplicationResolver(List<AlarmApplicationExistenceChecker> checkers) {
        Objects.requireNonNull(checkers, "checkers");

        Map<String, AlarmApplicationExistenceChecker> collected = new HashMap<>();
        for (AlarmApplicationExistenceChecker checker : checkers) {
            for (String applicationType : checker.supportedTypes()) {
                AlarmApplicationExistenceChecker previous = collected.putIfAbsent(applicationType, checker);
                if (previous != null) {
                    throw new IllegalStateException("Two checkers claim applicationType '"
                            + applicationType + "': " + previous.getClass().getName()
                            + " and " + checker.getClass().getName());
                }
            }
        }
        this.byType = Map.copyOf(collected);
    }

    public void verifyExists(AlarmApplication application) {
        requireComplete(application);

        AlarmApplicationExistenceChecker checker = byType.get(application.getApplicationType());
        if (checker == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported applicationType: " + application.getApplicationType());
        }
        if (!checker.exists(application)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Application not found: " + application);
        }
    }

    private static void requireComplete(AlarmApplication application) {
        if (application == null
                || !StringUtils.hasText(application.getServiceName())
                || !StringUtils.hasText(application.getApplicationName())
                || !StringUtils.hasText(application.getApplicationType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "serviceName, applicationName and applicationType are required");
        }
    }
}
