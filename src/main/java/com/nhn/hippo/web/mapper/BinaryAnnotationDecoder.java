package com.nhn.hippo.web.mapper;

import com.profiler.common.dto.thrift.BinaryAnnotation;
import com.profiler.common.dto.thrift.Span;

/**
 *
 */
public interface BinaryAnnotationDecoder {
    void decode(Span span);
}
