package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;

public interface InterceptorInjector {
    void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException;
}
