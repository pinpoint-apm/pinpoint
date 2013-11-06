package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * 
 */
public class Application {
	private final String applicationName;
	private final ServiceType serviceType;

	public Application(String applicationName, short serviceType) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        this.applicationName = applicationName;
		this.serviceType = ServiceType.findServiceType(serviceType);
	}

	public String getApplicationName() {
		return applicationName;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public String toString() {
		return applicationName + "(" + serviceType + ")";
	}
}
