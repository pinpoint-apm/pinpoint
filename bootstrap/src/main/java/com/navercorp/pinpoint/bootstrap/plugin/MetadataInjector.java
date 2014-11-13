package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;

public interface MetadataInjector {
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException;
}
