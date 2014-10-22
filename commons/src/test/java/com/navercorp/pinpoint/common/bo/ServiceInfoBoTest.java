package com.nhn.pinpoint.common.bo;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author hyungil.jeong
 */
public class ServiceInfoBoTest {

    @Test
    public void testByteArrayConversion() {
        // Given
        final ServiceInfoBo testBo = createTestBo("testService", Arrays.asList("lib1", "lib2"));
        // When
        final byte[] serializedBo = testBo.writeValue();
        final ServiceInfoBo deserializedBo = new ServiceInfoBo.Builder(serializedBo).build();
        // Then
        assertEquals(testBo, deserializedBo);
    }
    
    @Test
    public void testByteArrayConversionNullValues() {
        // Given
        final ServiceInfoBo testBo = createTestBo(null, null);
        // When
        final byte[] serializedBo = testBo.writeValue();
        final ServiceInfoBo deserializedBo = new ServiceInfoBo.Builder(serializedBo).build();
        // Then
        assertEquals(testBo, deserializedBo);
    }
    
    static ServiceInfoBo createTestBo(String serviceName, List<String> serviceLibs) {
        final ServiceInfoBo.Builder builder = new ServiceInfoBo.Builder();
        return builder.serviceName(serviceName).serviceLibs(serviceLibs).build();
    }

}
