package com.navercorp.pinpoint.bootstrap.instrument;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
