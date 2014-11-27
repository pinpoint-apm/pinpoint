package com.nhn.pinpoint.profiler.modifier;

import java.util.List;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPlugin;

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
