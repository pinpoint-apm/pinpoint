package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ResponseTimeViewModel.TimeCountSerializer.class)
public record TimeCount(long time, long count) {
}
