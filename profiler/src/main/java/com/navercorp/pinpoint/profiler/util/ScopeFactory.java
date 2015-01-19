package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public interface ScopeFactory {

    Scope createScope();

    String getName();

}
