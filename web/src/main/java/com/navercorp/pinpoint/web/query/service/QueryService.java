package com.navercorp.pinpoint.web.query.service;

public interface QueryService {
    BindType getBindType();
    String bind(String metaData, String bind);
}
