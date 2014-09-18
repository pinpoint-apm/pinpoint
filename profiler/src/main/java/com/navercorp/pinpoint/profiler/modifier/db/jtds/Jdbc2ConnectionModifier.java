package com.nhn.pinpoint.profiler.modifier.db.jtds;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;

/**
 * 1.2.x -> jdk 1.5
 * @author emeroad
 */
public class Jdbc2ConnectionModifier extends JtdsConnectionModifier {

    public Jdbc2ConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/ConnectionJDBC2";
    }
}
