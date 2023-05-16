package com.navercorp.pinpoint.bootstrap.instrument;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClassFiltersTest {

    @Test
    public void name() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.getName()).thenReturn("com.navercorp.mock.TestObjectNestedClass$InstanceInner");

        assertTrue(ClassFilters.name("com.navercorp.mock.TestObjectNestedClass$InstanceInner").accept(clazz));
        assertFalse(ClassFilters.name("com.navercorp.mock.InvalidClassName").accept(clazz));
        assertFalse(ClassFilters.name((String[]) null).accept(clazz));
        assertFalse(ClassFilters.name(null, null).accept(clazz));
    }

    @Test
    public void enclosingMethod() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.hasEnclosingMethod("call", "int")).thenReturn(Boolean.TRUE);

        assertTrue(ClassFilters.enclosingMethod("call", "int").accept(clazz));
        assertFalse(ClassFilters.enclosingMethod("invalid", "int").accept(clazz));
    }

    @Test
    public void interfaze() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.getInterfaces()).thenReturn(new String[]{"java.util.concurrent.Callable"});

        assertTrue(ClassFilters.interfaze("java.util.concurrent.Callable").accept(clazz));
        assertFalse(ClassFilters.interfaze("java.lang.Runnable").accept(clazz));
    }

    @Test
    public void chain() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.hasEnclosingMethod("call", "int")).thenReturn(Boolean.TRUE);
        when(clazz.getInterfaces()).thenReturn(new String[]{"java.util.concurrent.Callable"});

        assertTrue(ClassFilters.chain(ClassFilters.enclosingMethod("call", "int"), ClassFilters.interfaze("java.util.concurrent.Callable")).accept(clazz));
        assertFalse(ClassFilters.chain(ClassFilters.enclosingMethod("invalid", "int"), ClassFilters.interfaze("java.lang.Runnable")).accept(clazz));
    }
}
