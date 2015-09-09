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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MultiClassNameMatcher;
import com.navercorp.pinpoint.profiler.modifier.connector.asynchttpclient.AsyncHttpClientModifier;
import com.navercorp.pinpoint.profiler.modifier.log.log4j.LoggingEventOfLog4jModifier;
import com.navercorp.pinpoint.profiler.modifier.log.logback.LoggingEventOfLogbackModifier;
import com.navercorp.pinpoint.profiler.modifier.method.MethodModifier;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 * @author netspider
 * @author hyungil.jeong
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class DefaultModifierRegistry implements ModifierRegistry {

    // No concurrent issue because only one thread put entries to the map and get operations are started AFTER the map is completely build.
    // Set the map size big intentionally to keep hash collision low.
    private final Map<String, AbstractModifier> registry = new HashMap<String, AbstractModifier>(512);

    private final ByteCodeInstrumentor byteCodeInstrumentor;
    private final ProfilerConfig profilerConfig;
    private final Agent agent;

    public DefaultModifierRegistry(Agent agent, ByteCodeInstrumentor byteCodeInstrumentor) {
        this.agent = agent;
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.profilerConfig = agent.getProfilerConfig();
    }

    @Override
    public AbstractModifier findModifier(String className) {
        return registry.get(className);
    }

    public void addModifier(AbstractModifier modifier) {
        final Matcher matcher = modifier.getMatcher();
        // TODO extract matcher process
        if (matcher instanceof ClassNameMatcher) {
            final ClassNameMatcher classNameMatcher = (ClassNameMatcher)matcher;
            String className = classNameMatcher.getClassName();
            addModifier0(modifier, className);
        } else if (matcher instanceof MultiClassNameMatcher) {
            final MultiClassNameMatcher classNameMatcher = (MultiClassNameMatcher)matcher;
            List<String> classNameList = classNameMatcher.getClassNames();
            for (String className : classNameList) {
                addModifier0(modifier, className);
            }
        } else {
            throw new IllegalArgumentException("unsupported matcher :" + matcher);
        }
    }

    private void addModifier0(AbstractModifier modifier, String className) {
        // check jvmClassName
        final String checkJvmClassName = JavaAssistUtils.javaNameToJvmName(className);
        AbstractModifier old = registry.put(checkJvmClassName, modifier);
        if (old != null) {
            throw new IllegalStateException("Modifier already exist. className:" + checkJvmClassName + " new:" + modifier.getClass() + " old:" + old.getClass());
        }
    }

    public void addMethodModifier() {
        MethodModifier methodModifier = new MethodModifier(byteCodeInstrumentor, agent);
        addModifier(methodModifier);
    }

    public void addConnectorModifier() {
        // ning async http client
        addModifier(new AsyncHttpClientModifier(byteCodeInstrumentor, agent));
    }

    public void addLog4jModifier() {
        if (profilerConfig.isLog4jLoggingTransactionInfo()) {
            addModifier(new LoggingEventOfLog4jModifier(byteCodeInstrumentor, agent));
//            addModifier(new Nelo2AsyncAppenderModifier(byteCodeInstrumentor, agent));
//            addModifier(new NeloAppenderModifier(byteCodeInstrumentor, agent));
        }
    }

    public void addLogbackModifier() {
        if (profilerConfig.isLogbackLoggingTransactionInfo()) {
            addModifier(new LoggingEventOfLogbackModifier(byteCodeInstrumentor, agent));
        }
    }
}
