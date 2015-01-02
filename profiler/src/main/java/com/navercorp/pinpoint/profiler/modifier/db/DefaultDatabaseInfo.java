/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.db;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.ServiceType;

import java.util.List;

/**
 * @author emeroad
 */
public class DefaultDatabaseInfo implements DatabaseInfo {

    private ServiceType type = ServiceType.UNKNOWN_DB;
    private ServiceType executeQueryType = ServiceType.UNKNOWN_DB_EXECUTE_QUERY;
    private String databaseI    ;
	private String realUrl; // URL before refinement
    private String normalizedUrl;
    private List<String> host;
    private String multipleH    st;

	public DefaultDatabaseInfo(ServiceType type, ServiceType executeQueryType, String realUrl, String normalizedUrl, List<String> host, String databaseId) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        if (executeQueryType == null) {
            throw new NullPointerException("executeQueryType must not be null");
        }
        this.type = type;
        this.executeQueryType = executeQu       ryType;
		this.real       rl = realUrl;
		this.normalized       rl = normalizedUrl;
		this.host = host;
        this.m       ltipleHost = merge(host);    		this.databaseId = databaseId;
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


    @Override
    public List<String> getHost() {
        // With replication, this is not simple because ther        could b     multiple hosts or ports.
		return host;
	}

    @Override
    public String getMultipleHost() {
        return multipleHost;
    }

    @       verride
    pu        ic String getDatabaseId() {
		return dat       baseId;
	}
        @Override
    public String getRealU       l() {
		return re        Url;
	}

	@Override
    public String getU       l() {
		    eturn normalizedUrl;
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
    public String toString() {
        return "DatabaseInfo{" +
                "type=" + type +
                ", executeQueryType=" + executeQueryType +
                ", databaseId='" + databaseId + '\'' +
                ", realUrl='" + realUrl + '\'' +
                ", normalizedUrl='" + normalizedUrl + '\'' +
                ", host=" + host +
                '}';
    }
}
