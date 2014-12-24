/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

/**
 * @author emeroad
 */
public abstract class AbstractModifier implements Modifier {

    protected final ByteCodeInstrumentor byteCodeInstrumentor;
    protected final Agent agent;

    public AbstractModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        if (byteCodeInstrumentor == null) {
            throw new NullPointerException("byteCodeInstrumentor must not be null");
        }
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.agent = agent;
    }

    public Agent getAgent() {
        return agent;
    }
    
    public abstract String getTargetClass();
}
