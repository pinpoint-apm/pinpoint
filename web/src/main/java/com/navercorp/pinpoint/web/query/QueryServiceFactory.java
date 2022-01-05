package com.navercorp.pinpoint.web.query;

import io.jsonwebtoken.lang.Assert;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Component
public class QueryServiceFactory {

    private final Map<BindType, QueryService> serviceMap;

    public QueryServiceFactory(QueryService[] services) {
        Objects.requireNonNull(services, "services");
        this.serviceMap = buildMap(services);
    }

    private Map<BindType, QueryService> buildMap(QueryService[] services) {
        Map<BindType, QueryService> map = new EnumMap<>(BindType.class);
        for (QueryService service : services) {
            final QueryService duplicate = map.put(service.getBindType(), service);
            Assert.isNull(duplicate, "Duplicate BindType");
        }
        return map;
    }


    public QueryService getService(BindType bindType) {
        Objects.requireNonNull(bindType, "bindType");

        final QueryService queryService = serviceMap.get(bindType);
        if (queryService == null) {
            throw new IllegalArgumentException("Unknown BindType" + bindType);
        }
        return queryService;
    }
}
