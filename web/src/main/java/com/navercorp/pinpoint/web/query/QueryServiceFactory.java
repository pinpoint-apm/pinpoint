package com.navercorp.pinpoint.web.query;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Stream;

@Component
public class QueryServiceFactory {

    private final QueryService[] queryServices;

    public QueryServiceFactory(QueryService[] services) {
        Objects.requireNonNull(services, "services");

        this.queryServices = Stream.of(services)
                .map(this::wrapFilter)
                .toArray(QueryService[]::new);
    }

    private QueryService wrapFilter(QueryService service) {
        return new EscapeJsonFilter(service);
    }

    public QueryService getService(BindType bindType) {
        Objects.requireNonNull(bindType, "bindType");

        for (QueryService queryService : queryServices) {
            if (queryService.getBindType().equals(bindType)) {
                return queryService;
            }
        }
        throw new IllegalArgumentException("Unknown BindType" + bindType);
    }
}
