package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.instrument.ScopeDefinition;

/**
 * @author emeroad
 */
public interface ScopePool {
    Scope getScope(ScopeDefinition scopeDefinition);
}
