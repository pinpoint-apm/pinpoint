package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class Host {
	/**
	 * UI에서 호스트를 구분하기 위한 목적으로 hostname, agentid, endpoint등 구분할 수 있는 아무거나 넣으면 됨.
	 */
	private final String host;
	private final ServiceType serviceType;
	private final Histogram histogram;

	public Host(String host, ServiceType serviceType) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.host = host;
		this.serviceType = serviceType;
		this.histogram = new Histogram(serviceType);
	}

    public Host(Host copyHost) {
        if (copyHost == null) {
            throw new NullPointerException("copyHost must not be null");
        }

        this.host = copyHost.host;
        this.serviceType = copyHost.serviceType;
        this.histogram = new Histogram(serviceType);
        this.histogram.add(copyHost.histogram);
    }

    public String getHost() {
		return host;
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(host).append("\",");
		sb.append("\"histogram\":").append(histogram.getJson());
		sb.append("}");
		return sb.toString();
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Host{");
        sb.append("host='").append(host).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", ").append(histogram);
        sb.append('}');
        return sb.toString();
    }
}
