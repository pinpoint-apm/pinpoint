package com.nhn.pinpoint.profiler.plugin;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactoryMapping;
import com.nhn.pinpoint.bootstrap.plugin.PluginClassLoaderFactory;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

public class ClassEditorAdaptor extends AbstractModifier {
    private final ClassEditorFactoryMapping mapping;
    private final ProfilerPluginContext pluginContext;
    private final PluginClassLoaderFactory classLoaderFactory;

    
    public ClassEditorAdaptor(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, ClassEditorFactoryMapping mapping, ProfilerPluginContext pluginContext, PluginClassLoaderFactory classLoaderFactory) {
        super(byteCodeInstrumentor, agent);
        this.mapping = mapping;
        this.pluginContext = pluginContext;
        this.classLoaderFactory = classLoaderFactory;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        ClassLoader forPlugin = classLoaderFactory.get(classLoader);
        
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(forPlugin);
        
        try {
            Class<?> editorFactoryClass = forPlugin.loadClass(mapping.getEditorFactoryClassName());
            
            if (!ClassEditorFactory.class.isAssignableFrom(editorFactoryClass)) {
                throw new PinpointException("Illegal class editor factory mapping. factory class[" + editorFactoryClass + "] did not implent ClassEditorFactory");
            }
            
            ClassEditorFactory editorFactory = (ClassEditorFactory)editorFactoryClass.newInstance();
            ClassEditor editor = editorFactory.get(pluginContext);
            
            InstrumentClass target = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);
            
            return editor.edit(classLoader, target);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke plugin class editor for " + mapping.getTargetClassName() + ", editor factory: " + mapping.getEditorFactoryClassName(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        
    }

    @Override
    public String getTargetClass() {
        return mapping.getTargetClassName();
    }
}
