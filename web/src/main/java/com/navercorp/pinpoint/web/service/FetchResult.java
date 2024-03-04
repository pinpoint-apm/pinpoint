package com.navercorp.pinpoint.web.service;

public record FetchResult<T>(int fetchCount, T data) {
}
