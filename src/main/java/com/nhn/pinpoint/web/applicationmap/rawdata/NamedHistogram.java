package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class NamedHistogram extends Histogram {
    private final String name;

    public NamedHistogram(String name, ServiceType serviceType) {
        super(serviceType);
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        this.name = name;
    }

    public NamedHistogram(String name, short serviceType) {
        super(serviceType);
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        this.name = name;
    }


}
