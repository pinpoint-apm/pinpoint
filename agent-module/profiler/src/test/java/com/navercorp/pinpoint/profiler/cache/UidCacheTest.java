package com.navercorp.pinpoint.profiler.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UidCacheTest {
    UidCache sut;

    @Mock
    Function<String, byte[]> uidFunction;

    @BeforeEach
    void setUp() {
        sut = new UidCache(1024, uidFunction);

        when(uidFunction.apply(any()))
                .thenReturn(new byte[]{});
    }

    @Test
    void sameValue() {
        Result<byte[]> result1 = sut.put("test");
        Result<byte[]> result2 = sut.put("test");

        assertTrue(result1.isNewValue());
        assertFalse(result2.isNewValue());

        verify(uidFunction, times(1)).apply("test");
    }

    @Test
    void differentValue() {
        Result<byte[]> result1 = sut.put("test");
        Result<byte[]> result2 = sut.put("different");

        assertTrue(result1.isNewValue());
        assertTrue(result2.isNewValue());

        verify(uidFunction, times(1)).apply("test");
        verify(uidFunction, times(1)).apply("different");
    }
}
