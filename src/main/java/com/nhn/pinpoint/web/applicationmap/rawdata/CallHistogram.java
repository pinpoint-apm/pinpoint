package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

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

    private ObjectMapper MAPPER = new ObjectMapper();

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

    @JsonProperty("name")
    public String getId() {
		return id;
	}

    @JsonIgnore
	public ServiceType getServiceType() {
		return serviceType;
	}

    @JsonProperty("histogram")
	public Histogram getHistogram() {
		return histogram;
	}

    @JsonIgnore
	public String getJson() {
        try {
            // 추후 오브젝트를 MAPPER에 던저게 되면 해당 api를 제거하도록 하자.
            return MAPPER.writeValueAsString(this);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
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
