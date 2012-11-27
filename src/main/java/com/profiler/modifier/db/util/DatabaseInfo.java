package com.profiler.modifier.db.util;

import com.profiler.common.ServiceType;

/**
 *
 */
public class DatabaseInfo {

	ServiceType type = ServiceType.UNKNOWN;
	String databaseId;
	// 입력된 url을 보정하지 않은 값
	String realUrl;
	String normalizedUrl;
	String host;
	String port;

	public DatabaseInfo(ServiceType type, String realUrl, String normalizedUrl, String host, String port, String databaseId) {
		this.type = type;
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

	@Override
	public String toString() {
		return "DatabaseInfo{" + "type=" + type + ", databaseId='" + databaseId + '\'' + ", realUrl='" + realUrl + '\'' + ", normalizedUrl='" + normalizedUrl + '\'' + ", host='" + host + '\'' + ", port='" + port + '\'' + '}';
	}
}
