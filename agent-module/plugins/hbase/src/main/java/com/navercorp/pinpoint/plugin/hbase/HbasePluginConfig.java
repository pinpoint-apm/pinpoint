/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * The type Hbase plugin config.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018 /10/12
 */
public class HbasePluginConfig {

    private final boolean clientProfile;
    private final boolean adminProfile;
    private final boolean tableProfile;
    private final boolean paramsProfile;
    private final boolean tableNameProfile;
    private final boolean dataSizeProfile;

    /**
     * Instantiates a new Hbase plugin config.
     *
     * @param config the config
     */
    public HbasePluginConfig(ProfilerConfig config) {
        this.clientProfile = config.readBoolean(HbasePluginConstants.HBASE_CLIENT_CONFIG, true);
        this.adminProfile = config.readBoolean(HbasePluginConstants.HBASE_CLIENT_ADMIN_CONFIG, true);
        this.tableProfile = config.readBoolean(HbasePluginConstants.HBASE_CLIENT_TABLE_CONFIG, true);
        this.paramsProfile = config.readBoolean(HbasePluginConstants.HBASE_CLIENT_PARAMS_CONFIG, false);
        this.tableNameProfile = config.readBoolean(HbasePluginConstants.HBASE_CLIENT_TABLENAME_CONFIG, true);
        this.dataSizeProfile = config.readBoolean(HbasePluginConstants.HBASE_CLIENT_DATA_SIZE_CONFIG, false);
    }

    /**
     * Is client profile boolean.
     *
     * @return the boolean
     */
    public boolean isClientProfile() {
        return clientProfile;
    }

    /**
     * Is admin profile boolean.
     *
     * @return the boolean
     */
    public boolean isAdminProfile() {
        return adminProfile;
    }

    /**
     * Is table profile boolean.
     *
     * @return the boolean
     */
    public boolean isTableProfile() {
        return tableProfile;
    }

    /**
     * Is params profile boolean.
     *
     * @return the boolean
     */
    public boolean isParamsProfile() {
        return paramsProfile;
    }

    /**
     * Is tableName profile boolean.
     *
     * @return the boolean
     */
    public boolean isTableNameProfile() {
        return tableNameProfile;
    }

    /**
     * Is dataSize profile boolean.
     *
     * @return the boolean
     */
    public boolean isDataSizeProfile() {
        return dataSizeProfile;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HbasePluginConfig [clientProfile=");
        builder.append(clientProfile);
        builder.append(", adminProfile=");
        builder.append(adminProfile);
        builder.append(", tableProfile=");
        builder.append(tableProfile);
        builder.append(", paramsProfile=");
        builder.append(paramsProfile);
        builder.append(", dataSizeProfile=");
        builder.append(dataSizeProfile);
        builder.append("]");
        return builder.toString();
    }
}
