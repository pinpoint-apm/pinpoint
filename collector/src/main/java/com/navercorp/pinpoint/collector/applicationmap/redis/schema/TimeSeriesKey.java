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

import com.navercorp.pinpoint.redis.timeseries.protocol.Labels;

/**
 * @author intr3p1d
 */
public class TimeSeriesKey {
    // label can be filtering by redis

    // ts:tableKind:tenantId:mainServiceId:mainApplicationName:
    // subServiceId:subApplicationName:subServiceTypeSlot

    // record callCount as timeseries value
    // rowTimeSlot will be calculated after the data is inserted (redis -> hbase)
    private final LabelToKey tableKind;
    private final LabelToKey tenantId;
    private final LabelToKey mainServiceId;
    private final LabelToKey mainApplicationName;
    private final LabelToKey mainServiceType;
    private final LabelToKey subServiceId;
    private final LabelToKey subApplicationName;
    private final LabelToKey subServiceType;
    private final LabelToKey slotNumber;

    public TimeSeriesKey(
            ApplicationMapTable tableKind, String tenantId,
            int mainServiceId, String mainApplicationName,
            short mainServiceType,
            int subServiceId, String subApplicationName,
            short subServiceType,
            short slotNumber
    ) {
        this.tableKind = new LabelToKey(TimeSeriesLabel.TABLE_KIND, tableKind.getTable());
        this.tenantId = new LabelToKey(TimeSeriesLabel.TENANT_ID, tenantId);
        this.mainServiceId = new LabelToKey(TimeSeriesLabel.MAIN_SERVICE_ID, String.valueOf(mainServiceId));
        this.mainApplicationName = new LabelToKey(TimeSeriesLabel.MAIN_APPLICATION_NAME, mainApplicationName);
        this.mainServiceType = new LabelToKey(TimeSeriesLabel.MAIN_SERVICE_TYPE, String.valueOf(mainServiceType));
        this.subServiceId = new LabelToKey(TimeSeriesLabel.SUB_SERVICE_ID, String.valueOf(subServiceId));
        this.subApplicationName = new LabelToKey(TimeSeriesLabel.SUB_APPLICATION_NAME, subApplicationName);
        this.subServiceType = new LabelToKey(TimeSeriesLabel.SUB_SERVICE_TYPE_SLOT, String.valueOf(subServiceType));
        this.slotNumber = new LabelToKey(TimeSeriesLabel.SUB_SLOT_NUMBER, String.valueOf(slotNumber));
    }

    public String getKey() {
        return "ts:" + tableKind.getValue() + ":" + tenantId.getValue()
                + ":" + mainServiceId.getValue() + ":" + mainApplicationName.getValue()
                + ":" + mainServiceType.getValue()
                + ":" + subServiceId.getValue() + ":" + subApplicationName.getValue()
                + ":" + subServiceType.getValue()
                + ":" + slotNumber.getValue();
    }

    public Labels toLabels() {
        Labels labels = new Labels();
        labels.addLabel(tableKind.getLabel(), tableKind.getValue());
        labels.addLabel(tenantId.getLabel(), tenantId.getValue());
        labels.addLabel(mainServiceId.getLabel(), mainServiceId.getValue());
        labels.addLabel(mainApplicationName.getLabel(), mainApplicationName.getValue());
        labels.addLabel(mainServiceType.getLabel(), mainServiceType.getValue());
        labels.addLabel(subServiceId.getLabel(), subServiceId.getValue());
        labels.addLabel(subApplicationName.getLabel(), subApplicationName.getValue());
        labels.addLabel(subServiceType.getLabel(), subServiceType.getValue());
        labels.addLabel(slotNumber.getLabel(), slotNumber.getValue());
        return labels;
    }
}
