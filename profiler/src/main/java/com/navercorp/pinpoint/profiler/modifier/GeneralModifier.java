package com.nhn.pinpoint.profiler.modifier;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.util.bytecode.BytecodeClass;

public interface GeneralModifier extends Modifier {
    public boolean canModify(BytecodeClass target);
    public byte[] modify(ClassLoader loader, ProtectionDomain protectionDomain, BytecodeClass target, byte[] bytecodeBuffer);
}
