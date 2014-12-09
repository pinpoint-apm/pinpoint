package com.navercorp.pinpoint.profiler.modifier;

import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;

/**
 * ModifierProvider is a temporary interface to provide additional modifiers to Pinpoint profiler.
 * This will be replaced {@link ProfilerPlugin} later.
 * 
 * @deprecated
 * @author lioolli
 */
@Deprecated
public interface ModifierProvider {
    public List<Modifier> getModifiers(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent);
}
