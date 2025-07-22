package com.navercorp.pinpoint.service.dao;

import com.navercorp.pinpoint.service.vo.ServiceEntry;
import com.navercorp.pinpoint.service.vo.ServiceInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmptyServiceDao implements ServiceDao {

    @Override
    public int insertService(int uid, String name, Map<String, String> configuration) {
        throw new UnsupportedOperationException("insertService is not supported in EmptyServiceDao");
    }

    @Override
    public List<String> selectServiceNames() {
        return Collections.emptyList();
    }

    @Override
    public List<ServiceEntry> selectServiceList(int limit) {
        return Collections.emptyList();
    }

    @Override
    public ServiceInfo selectServiceInfo(String name) {
        return null;
    }

    @Override
    public ServiceEntry selectServiceEntry(String name) {
        return null;
    }

    @Override
    public ServiceEntry selectServiceEntry(int uid) {
        return null;
    }

    @Override
    public void updateServiceConfig(int uid, Map<String, String> configuration) {
        throw new UnsupportedOperationException("updateServiceConfig is not supported in EmptyServiceDao");
    }

    @Override
    public void updateServiceName(int uid, String name) {
        throw new UnsupportedOperationException("updateServiceName is not supported in EmptyServiceDao");
    }

    @Override
    public void deleteService(int uid) {
        throw new UnsupportedOperationException("insertService is not supported in EmptyServiceDao");
    }
}
