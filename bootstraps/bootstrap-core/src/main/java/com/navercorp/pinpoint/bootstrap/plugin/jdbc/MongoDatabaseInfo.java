/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.List;

/**
 * @author Roy Kim
 */
public class MongoDatabaseInfo implements DatabaseInfo {

    private final ServiceType type;
    private final ServiceType executeQueryType;
    private final String databaseId;
    private final String realUrl; // URL BEFORE refinement
    private final String normalizedUrl;
    private final List<String> host;
    private final String multipleHost;
    private final boolean parsingComplete;
    private final String collectionName;
    private final String readPreference;
    private final String writeConcern;

    public MongoDatabaseInfo(ServiceType type, ServiceType executeQueryType, String realUrl, String normalizedUrl, List<String> host, String databaseId, String collectionName, String readWriteOption, String writeConcern) {
        this(type, executeQueryType, realUrl, normalizedUrl, host, databaseId, collectionName, true, readWriteOption, writeConcern);
    }

    public MongoDatabaseInfo(ServiceType type, ServiceType executeQueryType, String realUrl, String normalizedUrl, List<String> host, String databaseId, String collectionName, boolean parsingComplete, String readPreference, String writeConcern) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (executeQueryType == null) {
            throw new NullPointerException("executeQueryType");
        }
        this.type = type;
        this.executeQueryType = executeQueryType;
        this.realUrl = realUrl;
        this.normalizedUrl = normalizedUrl;
        this.host = host;
        this.multipleHost = merge(host);
        this.databaseId = databaseId;
        this.parsingComplete = parsingComplete;
        this.readPreference = readPreference;
        this.writeConcern = writeConcern;
        this.collectionName = collectionName;
    }

    private String merge(List<String> host) {
        if (host.isEmpty()) {
            return "";
        }
        String single = host.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(single);
        for(int i =1; i<host.size(); i++) {
            sb.append(',');
            sb.append(host.get(i));
        }
        return sb.toString();
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getReadPreference() {
        return readPreference;
    }

    public String getWriteConcern() {
        return writeConcern;
    }

    @Override
    public List<String> getHost() {
        // With replication, this is not simple because there could be multiple hosts or ports.
        return host;
    }

    @Override
    public String getMultipleHost() {
        return multipleHost;
    }

    @Override
    public String getDatabaseId() {
        return databaseId;
    }

    @Override
    public String getRealUrl() {
        return realUrl;
    }

    @Override
    public String getUrl() {
        return normalizedUrl;
    }

    @Override
    public ServiceType getType() {
        return type;
    }

    @Override
    public ServiceType getExecuteQueryType() {
        return executeQueryType;
    }

    @Override
    public boolean isParsingComplete() {
        return parsingComplete;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoDatabaseInfo{");
        sb.append("type=").append(type);
        sb.append(", executeQueryType=").append(executeQueryType);
        sb.append(", databaseId='").append(databaseId).append('\'');
        sb.append(", realUrl='").append(realUrl).append('\'');
        sb.append(", normalizedUrl='").append(normalizedUrl).append('\'');
        sb.append(", host=").append(host);
        sb.append(", multipleHost='").append(multipleHost).append('\'');
        sb.append(", parsingComplete=").append(parsingComplete);
        sb.append(", readPreference=").append(readPreference);
        sb.append(", writeConcern=").append(writeConcern);
        sb.append(", collectionName=").append(collectionName);
        sb.append('}');
        return sb.toString();
    }

}
