/**
 * Copyright 2014 NAVER Corp.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditor;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class ClassEditorExecutor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ByteCodeInstrumentor instrumentor;
    private final PluginClassLoaderFactory classLoaderFactory;

    public ClassEditorExecutor(ByteCodeInstrumentor instrumentor, PluginClassLoaderFactory classLoaderFactory) {
        this.instrumentor = instrumentor;
        this.classLoaderFactory = classLoaderFactory;
    }

    public byte[] execute(ClassEditor editor, ClassLoader classLoader, String className, byte[] classFileBuffer) {
        ClassLoader forPlugin = classLoaderFactory.get(classLoader);
        
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(forPlugin);
        
        try {
            InstrumentClass target = instrumentor.getClass(classLoader, className, classFileBuffer);
            return editor.edit(forPlugin, target);
        } catch (PinpointException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Fail to invoke plugin class editor " + editor;
            logger.warn(msg, e);
            throw new PinpointException(msg, e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
