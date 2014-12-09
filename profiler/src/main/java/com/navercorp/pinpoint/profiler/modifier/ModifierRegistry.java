package com.navercorp.pinpoint.profiler.modifier;

/**
 * @author emeroad
 */
public interface ModifierRegistry {

    AbstractModifier findModifier(String className);

}
