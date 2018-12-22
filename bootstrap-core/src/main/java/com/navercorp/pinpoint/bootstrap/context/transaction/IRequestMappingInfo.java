package com.navercorp.pinpoint.bootstrap.context.transaction;

public interface IRequestMappingInfo {
    boolean match(String uri, String method);
    String getTransactionType();
}
