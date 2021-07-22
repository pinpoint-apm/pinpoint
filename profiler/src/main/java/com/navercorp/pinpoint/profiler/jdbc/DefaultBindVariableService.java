package com.navercorp.pinpoint.profiler.jdbc;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindVariableService;

import java.util.Map;
import java.util.Objects;

public class DefaultBindVariableService implements BindVariableService {
    private final BindValueConverter bindValueConverter;


    public DefaultBindVariableService(BindValueConverter bindValueConverter) {
        this.bindValueConverter = Objects.requireNonNull(bindValueConverter, "bindValueConverter");
    }

    @Override
    public String formatBindVariable(String methodName, Object[] args) {
        return bindValueConverter.convert(methodName, args);
    }

    @Override
    public String bindVariableToString(Map<Integer, String> bindValueMap, int limit) {
        return BindValueUtils.bindValueToString(bindValueMap, limit);
    }
}
