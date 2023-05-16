package com.navercorp.pinpoint.web.query.model;

import java.util.Objects;

public class BindSqlView {
    private final String bindedQuery;

    public BindSqlView(String bindedQuery) {
        this.bindedQuery = Objects.requireNonNull(bindedQuery, "bindedQuery");
    }

    public String getBindedQuery() {
        return bindedQuery;
    }
}
