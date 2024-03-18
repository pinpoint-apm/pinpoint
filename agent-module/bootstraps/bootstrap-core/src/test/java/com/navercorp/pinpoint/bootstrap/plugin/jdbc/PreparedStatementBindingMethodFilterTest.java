package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreparedStatementBindingMethodFilterTest {

    @Test
    public void test_array_parameter() {
        PreparedStatementBindingMethodFilter filter = PreparedStatementBindingMethodFilter.includes("setBytes");
        InstrumentMethod byteMethod = mock(InstrumentMethod.class);
        // setBytes(int parameterIndex, byte x[])
        when(byteMethod.getName()).thenReturn("setBytes");
        when(byteMethod.getParameterTypes()).thenReturn(new String[]{"int", "byte[]"});

        Assertions.assertTrue(filter.accept(byteMethod));
    }


}