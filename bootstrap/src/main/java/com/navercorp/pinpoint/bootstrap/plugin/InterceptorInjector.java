package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;

public interface InterceptorInjector {
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException;
}
