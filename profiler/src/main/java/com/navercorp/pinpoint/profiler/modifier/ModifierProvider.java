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

import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;

/**
 * ModifierProvider is a temporary interface to provide additional modifiers to Pinpoint profiler.
 * This will be replaced by {@link ProfilerPlugin} later.
 * 
 * @deprecated
 * @author lioolli
 */
@Deprecated
public interface ModifierProvider {
    List<Modifier> getModifiers(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent);
}
