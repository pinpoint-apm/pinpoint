package com.navercorp.pinpoint.bootstrap.instrument;

import java.lang.reflect.Modifier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class MethodFiltersTest {

    @Test
    public void name() {
        InstrumentMethod method = mock(InstrumentMethod.class);
        when(method.getName()).thenReturn("call");
        
        assertTrue(MethodFilters.name("call").accept(method));
        assertFalse(MethodFilters.name("execute").accept(method));
        assertFalse(MethodFilters.name().accept(method));
        assertFalse(MethodFilters.name((String[]) null).accept(method));
        assertFalse(MethodFilters.name(null, null).accept(method));
    }
    
    @Test
    public void modifier() {
        InstrumentMethod method = mock(InstrumentMethod.class);
        //  modifier is public abstract.
        when(method.getModifiers()).thenReturn(1025);
        
        assertTrue(MethodFilters.modifier(Modifier.PUBLIC).accept(method));
        assertTrue(MethodFilters.modifier(Modifier.ABSTRACT).accept(method));
        assertFalse(MethodFilters.modifier(Modifier.FINAL).accept(method));
    }
}
