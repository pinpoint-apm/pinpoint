package com.profiler.modifier.db.util;

import com.profiler.common.ServiceType;

import java.util.List;

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
    private List<String> host;
    private String multipleHost;

	public DatabaseInfo(ServiceType type, ServiceType executeQueryType, String realUrl, String normalizedUrl, List<String> host, String databaseId) {
		this.type = type;
        this.executeQueryType = executeQueryType;
		this.realUrl = realUrl;
		this.normalizedUrl = normalizedUrl;
		this.host = host;
        this.multipleHost = merge(host);
		this.databaseId = databaseId;
	}

    private String merge(List<java.lang.String> host) {
        if (host.size() == 0) {
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


    public List<String> getHost() {
		// host와 port의 경우 replication 설정등으로 n개가 될수 있어 애매하다.
		return host;
	}

    public String getMultipleHost() {
        return multipleHost;
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
                ", host=" + host +
                '}';
    }
}
