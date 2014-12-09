package com.navercorp.pinpoint.profiler.modifier.db.jtds;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

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
