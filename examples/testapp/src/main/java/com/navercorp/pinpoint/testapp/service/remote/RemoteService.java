package com.navercorp.pinpoint.testapp.service.remote;

import java.util.Map;

public interface RemoteService {

    public Map<String, Object> getGeoCode(String address);
    
    public Map<String, Object> getTwitterUrlCount(String url);
}
