package com.nhn.pinpoint.profiler.modifier;

/**
 * @author emeroad
 */
public interface ModifierRegistry {

    AbstractModifier findModifier(String className);

}
