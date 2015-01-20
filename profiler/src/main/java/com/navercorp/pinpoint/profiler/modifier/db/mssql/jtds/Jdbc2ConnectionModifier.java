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

package com.navercorp.pinpoint.profiler.modifier.db.mssql.jtds;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.modifier.db.mssql.MsSqlConnectionModifier;

/**
 * 1.2.x -> jdk 1.5
 *
 * @author emeroad
 */
public class Jdbc2ConnectionModifier extends MsSqlConnectionModifier {

    public Jdbc2ConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/ConnectionJDBC2";
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.modifier.db.Scopeable#getScopeName()
     */
    @Override
    public String getScopeName() {
        return JtdsScope.SCOPE_NAME;
    }
}
