package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;

public interface Condition {
    public boolean check(InstrumentClass target);
}
