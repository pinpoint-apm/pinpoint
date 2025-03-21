/*
 * Copyright 2024 NAVER Corp.
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
 */
package com.navercorp.pinpoint.collector.applicationmap.redis.schema;

/**
 * @author intr3p1d
 */
public enum TimeSeriesLabel {
    TABLE_KIND("tableKind"),
    TENANT_ID("tenantId"),
    MAIN_SERVICE_ID("mainServiceId"),
    MAIN_APPLICATION_NAME("mainApplicationName"),
    MAIN_SERVICE_TYPE("mainServiceType"),
    SUB_SERVICE_ID("subServiceId"),
    SUB_APPLICATION_NAME("subApplicationName"),
    SUB_SERVICE_TYPE_SLOT("subServiceTypeSlot"),
    SUB_SLOT_NUMBER("subSlotNumber");

    private final String label;

    TimeSeriesLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
