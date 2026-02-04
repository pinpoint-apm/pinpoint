package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;

public interface AnnotationWriter {
    void write(AnnotationBo annotationBo);
}
