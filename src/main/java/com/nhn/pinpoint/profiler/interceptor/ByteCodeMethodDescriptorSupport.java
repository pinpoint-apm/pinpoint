package com.nhn.pinpoint.profiler.interceptor;

/**
 * precompile level의 methodDescriptor를 setting 받을수 있게 한다.
 * @author emeroad
 */
public interface ByteCodeMethodDescriptorSupport {
    void setMethodDescriptor(MethodDescriptor descriptor);
}
