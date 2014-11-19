package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;

public interface ClassEditor {
    public byte[] edit(ClassLoader classLoader, InstrumentClass target);
}
