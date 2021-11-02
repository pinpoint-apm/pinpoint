package com.navercorp.pinpoint.web.service;

public class FetchResult<T> {
    private final int fetchCount;
    private final T data;

    public FetchResult(int fetchCount, T data) {
        this.fetchCount = fetchCount;
        this.data = data;
    }

    public int getFetchCount() {
        return fetchCount;
    }

    public T getData() {
        return data;
    }
}
