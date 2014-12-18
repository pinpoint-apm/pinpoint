package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;

public interface Condition {
    boolean check(InstrumentClass target);
}
