package com.navercorp.pinpoint.common.util;

/**
 * @author emeroad
 */
public class DefaultNormalizedSql implements NormalizedSql {

    private final String normalizedSql;
    private final String parseParameter;

    public DefaultNormalizedSql(String normalizedSql, String parseParameter) {
        this.normalizedSql = normalizedSql;
        this.parseParameter = parseParameter;
    }

    @Override
    public String getNormalizedSql() {
        return normalizedSql;
    }

    @Override
    public String getParseParameter() {
        return parseParameter;
    }
}