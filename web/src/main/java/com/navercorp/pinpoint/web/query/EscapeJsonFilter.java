package com.navercorp.pinpoint.web.query;

import org.apache.commons.text.StringEscapeUtils;

import java.util.Objects;

public class EscapeJsonFilter implements QueryService {
    private final QueryService delegate;

    public EscapeJsonFilter(QueryService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public String bind(String metaData, String bind) {
        String bindedQuery = delegate.bind(metaData, bind);
        return StringEscapeUtils.escapeJson(bindedQuery);
    }

    @Override
    public BindType getBindType() {
        return delegate.getBindType();
    }
}
