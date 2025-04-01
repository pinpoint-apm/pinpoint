/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.servermap.bo;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class DirectionalBo {
//    ts:tableKind:tenantId
//    :mainServiceId:mainApplicationName:mainServiceType
//    :subServiceId:subApplicationName:subServiceType
//    :(main)slotNumber

    private final TableName tableName;
    private final String tenantId;
    private final int mainServiceId;
    private final String mainApplicationName;
    private final short mainServiceType;
    private final int subServiceId;
    private final String subApplicationName;
    private final short subServiceType;
    private final short slotNumber;

    private List<CallCount> callCountList;


    public DirectionalBo(
            String tableName,
            String tenantId,
            int mainServiceId, String mainApplicationName, short mainServiceType,
            int subServiceId, String subApplicationName, short subServiceType,
            short slotNumber
    ) {
        Objects.requireNonNull(tableName, "tableName");
        this.tableName = TableName.of(tableName);
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.mainServiceId = mainServiceId;
        this.mainApplicationName = Objects.requireNonNull(mainApplicationName, "mainApplicationName");
        this.mainServiceType = mainServiceType;
        this.subServiceId = subServiceId;
        this.subApplicationName = Objects.requireNonNull(subApplicationName, "subApplicationName");
        this.subServiceType = subServiceType;
        this.slotNumber = slotNumber;
    }

    public static DirectionalBo fromKey(String key) {
        String[] parts = key.split(":");
        if (!Objects.equals(parts[0], "ts")) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }
        if (parts.length != 10) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }

        return new DirectionalBo(
                parts[1], parts[2],
                Integer.parseInt(parts[3]), parts[4], Short.parseShort(parts[5]),
                Integer.parseInt(parts[6]), parts[7], Short.parseShort(parts[8]),
                Short.parseShort(parts[9])
        );
    }

    public TableName getTableName() {
        return tableName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public int getMainServiceId() {
        return mainServiceId;
    }

    public String getMainApplicationName() {
        return mainApplicationName;
    }

    public short getMainServiceType() {
        return mainServiceType;
    }

    public int getSubServiceId() {
        return subServiceId;
    }

    public String getSubApplicationName() {
        return subApplicationName;
    }

    public short getSubServiceType() {
        return subServiceType;
    }

    public short getSlotNumber() {
        return slotNumber;
    }

    public List<CallCount> getCallCountList() {
        return callCountList;
    }

    public void setCallCountList(List<CallCount> callCountList) {
        this.callCountList = callCountList;
    }

    @Override
    public String toString() {
        return "DirectionalBo{" +
                "tableName=" + tableName +
                ", tenantId='" + tenantId + '\'' +
                ", mainServiceId='" + mainServiceId + '\'' +
                ", mainApplicationName='" + mainApplicationName + '\'' +
                ", mainServiceType=" + mainServiceType +
                ", subServiceId='" + subServiceId + '\'' +
                ", subApplicationName='" + subApplicationName + '\'' +
                ", subServiceType=" + subServiceType +
                ", slotNumber=" + slotNumber +
                ", callCountList=" + callCountList +
                '}';
    }
}
