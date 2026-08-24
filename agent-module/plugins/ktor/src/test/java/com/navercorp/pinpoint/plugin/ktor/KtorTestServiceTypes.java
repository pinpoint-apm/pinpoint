/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.ktor;

import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataRegistrar;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registers the service types declared in this plugin's own
 * META-INF/pinpoint/type-provider.yml into ServiceTypeProvider, so unit tests
 * exercise KtorConstants against the same metadata the agent will ship.
 * Static initialisation is the single point of registration: regardless of
 * which test class loads first, the values always come from the yml file.
 */
public final class KtorTestServiceTypes {
    private static final String TYPE_PROVIDER_RESOURCE = "META-INF/pinpoint/type-provider.yml";
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\s*-\\s+code:\\s*(\\d+)\\s*$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\s+name:\\s*'?([A-Za-z0-9_]+)'?\\s*$");

    static {
        Map<String, ServiceType> byName = parseServiceTypes();
        TraceMetadataRegistrar.registerServiceTypes(new TypeProviderServiceTypeLocator(byName));
        // resolve now, outside any Mockito stubbing/verification window, so a
        // failed KtorConstants.<clinit> can never poison later test classes
        KtorConstants.KTOR.getName();
        KtorConstants.KTOR_INTERNAL.getName();
        KtorConstants.KTOR_CLIENT.getName();
        KtorConstants.KTOR_CLIENT_INTERNAL.getName();
    }

    private KtorTestServiceTypes() {
    }

    public static void register() {
        // trigger the static block; later calls are no-ops
    }

    private static Map<String, ServiceType> parseServiceTypes() {
        InputStream inputStream = KtorTestServiceTypes.class.getClassLoader().getResourceAsStream(TYPE_PROVIDER_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException(TYPE_PROVIDER_RESOURCE + " not found on the test classpath");
        }

        Map<String, ServiceType> byName = new LinkedHashMap<>();
        Integer pendingCode = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher codeMatcher = CODE_PATTERN.matcher(line);
                if (codeMatcher.matches()) {
                    pendingCode = Integer.valueOf(codeMatcher.group(1));
                    continue;
                }
                if (pendingCode == null) {
                    continue;
                }
                Matcher nameMatcher = NAME_PATTERN.matcher(line);
                if (nameMatcher.matches()) {
                    byName.put(nameMatcher.group(1), ServiceTypeFactory.of(pendingCode, nameMatcher.group(1)));
                    pendingCode = null;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse " + TYPE_PROVIDER_RESOURCE, e);
        }
        if (byName.isEmpty()) {
            throw new IllegalStateException("No serviceTypes parsed from " + TYPE_PROVIDER_RESOURCE);
        }
        return byName;
    }

    private static final class TypeProviderServiceTypeLocator implements ServiceTypeLocator {
        private final Map<String, ServiceType> byName;
        private final Map<Integer, ServiceType> byCode;

        TypeProviderServiceTypeLocator(Map<String, ServiceType> byName) {
            this.byName = byName;
            Map<Integer, ServiceType> codes = new LinkedHashMap<>();
            for (ServiceType serviceType : byName.values()) {
                codes.put((int) serviceType.getCode(), serviceType);
            }
            this.byCode = codes;
        }

        @Override
        public ServiceType findServiceType(int code) {
            ServiceType serviceType = byCode.get(code);
            return serviceType != null ? serviceType : ServiceType.UNDEFINED;
        }

        @Override
        public ServiceType findServiceTypeByName(String name) {
            ServiceType serviceType = byName.get(Objects.requireNonNull(name, "name"));
            return serviceType != null ? serviceType : ServiceType.UNDEFINED;
        }

        @Override
        public List<ServiceType> findDesc(String name) {
            return Collections.unmodifiableList(new ArrayList<>(byName.values()));
        }
    }
}
