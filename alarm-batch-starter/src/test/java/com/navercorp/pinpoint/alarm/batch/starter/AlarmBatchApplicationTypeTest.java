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

import com.navercorp.pinpoint.common.server.util.ServerTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A rule names the application type an operator picked, and what that name means is decided by
 * the plugins this deployable ships. Ship none and the built-in types still resolve, so the
 * process starts and evaluates; every other type resolves to undefined, and the stores keyed by
 * it answer with an empty histogram and no agents -- which a condition cannot tell apart from a
 * healthy application. Asserted here because the dependency that carries them has no other
 * caller in this module to break.
 */
class AlarmBatchApplicationTypeTest {

    private final ServiceTypeRegistryService registry = new DefaultServiceTypeRegistryService(
            new ServerTraceMetadataLoaderService().getServiceTypeLocator());

    @Test
    void theTypesAnOperatorPicksAreOnTheClasspath() {
        for (String typeName : List.of("TOMCAT", "SPRING_BOOT", "JETTY")) {
            assertNotEquals(ServiceType.UNDEFINED, registry.findServiceTypeByName(typeName),
                    typeName + " resolves to undefined: this deployable ships no plugin"
                            + " defining it, and a rule naming it would read an empty store");
        }
    }
}
