package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;

public interface Condition {
    public boolean check(InstrumentClass target);
}
