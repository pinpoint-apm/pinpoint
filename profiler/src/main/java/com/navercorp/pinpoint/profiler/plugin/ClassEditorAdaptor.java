package com.nhn.pinpoint.profiler.plugin;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.DedicatedClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.PluginClassLoaderFactory;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

public class ClassEditorAdaptor extends AbstractModifier {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DedicatedClassEditor editor;
    private final PluginClassLoaderFactory classLoaderFactory;

    
    public ClassEditorAdaptor(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, DedicatedClassEditor editor, PluginClassLoaderFactory classLoaderFactory) {
        super(byteCodeInstrumentor, agent);
        this.editor = editor;
        this.classLoaderFactory = classLoaderFactory;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        logger.debug("Editing class {}", className);
        
        ClassLoader forPlugin = classLoaderFactory.get(classLoader);
        
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(forPlugin);
        
        try {
            InstrumentClass target = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);
            return editor.edit(classLoader, target);
        } catch (PinpointException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Fail to invoke plugin class editor " + editor.getClass().getName() + " for " + editor.getTargetClassName();
            logger.warn(msg, e);
            throw new PinpointException(msg, e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        
    }

    @Override
    public String getTargetClass() {
        return editor.getTargetClassName().replace('.', '/');
    }
}
