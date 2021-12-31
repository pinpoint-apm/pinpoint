package com.navercorp.pinpoint.web.query;

public interface QueryService {
    BindType getBindType();
    String bind(String metaData, String bind);
}
