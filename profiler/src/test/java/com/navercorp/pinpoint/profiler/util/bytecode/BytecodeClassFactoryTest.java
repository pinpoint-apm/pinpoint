package com.nhn.pinpoint.profiler.util.bytecode;

import static org.junit.Assert.*;

import org.junit.Test;

public class BytecodeClassFactoryTest {

    @Test
    public void test() {
        BytecodeClass a = BytecodeClassFactory.get("org/junit/Assert", getClass().getClassLoader());
        assertEquals("org/junit/Assert", a.getName());
        
        BytecodeClass list = BytecodeClassFactory.get("java/util/List", getClass().getClassLoader());
        assertNotNull("java/util/List", list.getName());
    }

}
