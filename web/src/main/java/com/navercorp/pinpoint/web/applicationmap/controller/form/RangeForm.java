package com.navercorp.pinpoint.web.applicationmap.controller.form;

import com.navercorp.pinpoint.common.timeseries.time.Timestamp;

public class RangeForm {

    private final Timestamp from;
    private final Timestamp to;

    public RangeForm(Timestamp from, Timestamp to) {
        this.from = from;
        this.to = to;
    }

    public Timestamp getFrom() {
        return from;
    }

    public Timestamp getTo() {
        return to;
    }
}
