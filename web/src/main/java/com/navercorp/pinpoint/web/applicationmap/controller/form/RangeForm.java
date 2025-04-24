package com.navercorp.pinpoint.web.applicationmap.controller.form;

import jakarta.validation.constraints.PositiveOrZero;

public class RangeForm {

    @PositiveOrZero
    private long from;
    @PositiveOrZero
    private long to;

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }
}
