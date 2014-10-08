package com.nhn.pinpoint.profiler.modifier;

/**
 * @author emeroad
 */
public interface ModifierRegistry {

    DedicatedModifier findModifier(String className);

}
