package com.profiler.modifier.db.util;

import com.profiler.common.ServiceType;

/**
 *
 */
public class DatabaseInfo {

	private ServiceType type = ServiceType.UNKNOWN;
    private ServiceType executeQueryType = ServiceType.UNKNOWN;
    private String databaseId;
	// 입력된 url을 보정하지 않은 값
    private String realUrl;
    private String normalizedUrl;
    private String host;
    private String port;

	public DatabaseInfo(ServiceType type, ServiceType executeQueryType, String realUrl, String normalizedUrl, String host, String port, String databaseId) {
		this.type = type;
        this.executeQueryType = executeQueryType;
		this.realUrl = realUrl;
		this.normalizedUrl = normalizedUrl;
		this.host = host;
		this.port = port;
		this.databaseId = databaseId;
	}

	@Deprecated
	public String getHost() {
		// host와 port의 경우 replication 설정등으로 n개가 될수 있어 애매하다.
		return host;
	}

	@Deprecated
	public String getPort() {
		// host와 port의 경우 replication 설정등으로 n개가 될수 있어 애매하다.
		return port;
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public String getRealUrl() {
		return realUrl;
	}

	public String getUrl() {
		return normalizedUrl;
	}

	public ServiceType getType() {
		return type;
	}

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
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                '}';
    }


}
