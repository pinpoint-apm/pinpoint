package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.common.trace.MethodType;
import com.navercorp.pinpoint.common.util.LineNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DubboProviderMethodDescriptorTest {

    @Test
    public void test() {

        DubboProviderMethodDescriptor descriptor = new DubboProviderMethodDescriptor();

        Assertions.assertEquals(descriptor.getApiDescriptor(), "Dubbo Provider Process");
        Assertions.assertEquals(descriptor.getApiId(), 0);
        Assertions.assertNull(descriptor.getClassName());
        Assertions.assertEquals(descriptor.getFullName(), "com.navercorp.pinpoint.plugin.dubbo.DubboProviderMethodDescriptor.invoke()");

        Assertions.assertEquals(descriptor.getLineNumber(), LineNumber.NO_LINE_NUMBER);
        Assertions.assertNull(descriptor.getMethodName());

        Assertions.assertNull(descriptor.getParameterDescriptor());

        Assertions.assertArrayEquals(descriptor.getParameterTypes(), new String[0]);

        Assertions.assertArrayEquals(descriptor.getParameterVariableName(), new String[0]);
        Assertions.assertEquals(descriptor.getType(), MethodType.WEB_REQUEST);
    }
}