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

package com.navercorp.pinpoint.profiler.plugin;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.DedicatedClassEditor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

public class ClassEditorAdaptor extends AbstractModifier {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DedicatedClassEditor editor;
    private final ClassEditorExecutor classEditorExecutor;

    
    public ClassEditorAdaptor(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, DedicatedClassEditor editor, ClassEditorExecutor classEditorExecutor) {
        super(byteCodeInstrumentor, agent);
        this.editor = editor;
        this.classEditorExecutor = classEditorExecutor;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        logger.debug("Editing class {}", className);
        return classEditorExecutor.execute(editor, classLoader, className, classFileBuffer);
    }

    @Override
    public String getTargetClass() {
        return editor.getTargetClassName().replace('.', '/');
    }
}
