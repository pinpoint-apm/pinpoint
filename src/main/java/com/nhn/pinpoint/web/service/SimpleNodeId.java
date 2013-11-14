package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class SimpleNodeId extends Node implements NodeId {

    public SimpleNodeId(String name, ServiceType serviceType) {
        super(name, serviceType);
    }

    public Node getKey() {
        return this;
    }

    @Override
    public String toString() {
        return "SimpleNodeId:{" + super.toString() + "}";
    }
}
