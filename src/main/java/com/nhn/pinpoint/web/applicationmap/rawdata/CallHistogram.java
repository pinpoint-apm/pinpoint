package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class CallHistogram {
	/**
	 * UI에서 호스트를 구분하기 위한 목적으로 hostname, agentid, endpoint등 구분할 수 있는 아무거나 넣으면 됨.
	 */
	private final String id;
	private final ServiceType serviceType;
	private final Histogram histogram;

	public CallHistogram(String agent, ServiceType serviceType) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.id = agent;
		this.serviceType = serviceType;
		this.histogram = new Histogram(serviceType);
	}

    public CallHistogram(CallHistogram copyCallHistogram) {
        if (copyCallHistogram == null) {
            throw new NullPointerException("copyCallHistogram must not be null");
        }

        this.id = copyCallHistogram.id;
        this.serviceType = copyCallHistogram.serviceType;
        this.histogram = new Histogram(serviceType);
        this.histogram.add(copyCallHistogram.histogram);
    }

    public String getId() {
		return id;
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
		sb.append("\"name\":\"").append(id).append("\",");
		sb.append("\"histogram\":").append(histogram.getJson());
		sb.append("}");
		return sb.toString();
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallHistogram{");
        sb.append("agent='").append(id).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", ").append(histogram);
        sb.append('}');
        return sb.toString();
    }
}
