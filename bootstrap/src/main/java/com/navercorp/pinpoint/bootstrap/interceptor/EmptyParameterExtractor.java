package com.nhn.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
public class EmptyParameterExtractor implements ParameterExtractor {

    public static final ParameterExtractor INSTANCE = new EmptyParameterExtractor();

    private EmptyParameterExtractor() {
    }

    @Override
    public int getIndex() {
        return NOT_FOUND;
    }

    @Override
    public Object extractObject(Object[] parameterList) {
        return NULL;
    }
}
