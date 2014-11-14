package com.nhn.pinpoint.profiler.plugin;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.plugin.DedicatedClassEditor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

public class ClassEditorAdaptor extends AbstractModifier {
    private final DedicatedClassEditor editor;
    
    public ClassEditorAdaptor(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, DedicatedClassEditor editor) {
        super(byteCodeInstrumentor, agent);
        this.editor = editor;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        return editor.edit(classLoader, className, protectionDomain, classFileBuffer);
    }

    @Override
    public String getTargetClass() {
        return editor.getTargetClassName();
    }
}
