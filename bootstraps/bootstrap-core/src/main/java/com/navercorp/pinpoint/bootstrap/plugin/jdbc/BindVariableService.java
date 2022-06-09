package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import java.util.Map;

public interface BindVariableService {
    String formatBindVariable(String methodName, Object[] args);
    String formatBindVariable(Object value);

    String bindVariableToString(Map<Integer, String> bindValueMap, int limit);
    String bindNameVariableToString(Map<String, String> bindValueMap, int limit);
}
