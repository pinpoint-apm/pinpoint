package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
public class Application {
	private final String applicationName;
    private final ServiceType serviceType;
    // undefine일 경우 추적이 쉽도록 별도 데이터를 보관한다.
    private final short code;

    public Application(String applicationName, ServiceType serviceType) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.applicationName = applicationName;
        this.serviceType = serviceType;
        this.code = serviceType.getCode();
    }

	public Application(String applicationName, short serviceType) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        this.applicationName = applicationName;
		this.serviceType = ServiceType.findServiceType(serviceType);
        this.code = serviceType;
	}

    public String getApplicationName() {
		return applicationName;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

    public short getServiceTypeCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (!applicationName.equals(that.applicationName)) return false;
        if (serviceType != that.serviceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationName.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }

    @Override
	public String toString() {
		return applicationName + "(" + serviceType + ":" + code + ")";
	}
}
