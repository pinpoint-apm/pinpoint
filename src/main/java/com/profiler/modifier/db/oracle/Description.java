package com.profiler.modifier.db.oracle;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Description {
    private int addressIndex = -1;
    private ArrayList<Address> addressList = new ArrayList<Address>();
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void increaseAddressIndex() {
        addressIndex++;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<Address> getAddressList() {
        return addressList;
    }

    public void addProtocol(String protocol) {
        if (addressIndex >= addressList.size()) {
            // 공간 확장.
            addressList.add(new Address());
        }
        Address address = this.addressList.get(addressIndex);
        if (address != null) {
            addressList.set(addressIndex, address);
            return;
        }
        Address newAddress = new Address();
        newAddress.setProtocol(protocol);
        this.addressList.set(addressIndex, newAddress);
    }
    public void addHost(String host) {
//        this.add
    }

    public void addPort(String port) {
//        this.add
    }
}
