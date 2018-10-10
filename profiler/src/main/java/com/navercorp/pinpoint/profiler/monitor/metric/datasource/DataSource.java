/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.datasource;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DataSource {
    private final int id; // required
    private short serviceTypeCode; // optional
    private java.lang.String databaseName; // optional
    private java.lang.String url; // optional
    private int activeConnectionSize; // optional
    private int maxConnectionSize; // optional


    public DataSource(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    public short getServiceTypeCode() {
        return serviceTypeCode;
    }

    public void setServiceTypeCode(short serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getActiveConnectionSize() {
        return activeConnectionSize;
    }

    public void setActiveConnectionSize(int activeConnectionSize) {
        this.activeConnectionSize = activeConnectionSize;
    }

    public int getMaxConnectionSize() {
        return maxConnectionSize;
    }

    public void setMaxConnectionSize(int maxConnectionSize) {
        this.maxConnectionSize = maxConnectionSize;
    }
}
