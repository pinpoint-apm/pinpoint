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
package com.navercorp.pinpoint.plugin.jetty12;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * Shares ServiceType codes with the legacy jetty plugin's JettyConstants so
 * traces collected by jetty12 are rendered under the same JETTY application
 * type. ServiceType registration is performed by the legacy plugin's
 * TraceMetadataProvider; this class only re-declares the codes locally.
 */
public final class Jetty12Constants {
    private Jetty12Constants() {
    }

    public static final ServiceType JETTY = ServiceTypeFactory.of(1030, "JETTY", RECORD_STATISTICS);
    public static final ServiceType JETTY_METHOD = ServiceTypeFactory.of(1031, "JETTY_METHOD");
}
