package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;

public interface ClassEditor {
    public byte[] edit(ClassLoader classLoader, InstrumentClass target);
}
