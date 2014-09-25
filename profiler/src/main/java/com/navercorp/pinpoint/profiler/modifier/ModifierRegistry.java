package com.nhn.pinpoint.profiler.modifier;

/**
 * @author emeroad
 */
public interface ModifierRegistry {

    Modifier findModifier(String className);

}
