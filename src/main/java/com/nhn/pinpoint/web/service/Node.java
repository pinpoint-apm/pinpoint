package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class Node {
    private String name;
    private ServiceType serviceType;

    public Node(String name, ServiceType serviceType) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.name = name;
        this.serviceType = serviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        this.name = name;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }

        this.serviceType = serviceType;
    }

    public boolean isLink() {
        // record해야 되거나. rpc콜은 링크이다.
        return !serviceType.isRecordStatistics() || serviceType.isRpcClient();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!name.equals(node.name)) return false;
        if (serviceType != node.serviceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Node{");
        sb.append("name='").append(name).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append('}');
        return sb.toString();
    }
}
