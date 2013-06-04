package com.nhn.pinpoint.profiler.modifier;

public interface ModifierRegistry {

    Modifier findModifier(String className);

}
