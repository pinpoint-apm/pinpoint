package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FastjsonMetadataProviderTest {

    @Test
    public void setup() {

        FastjsonMetadataProvider provider = new FastjsonMetadataProvider();

        provider.setup(new TraceMetadataSetupContext() {

            @Override
            public void addServiceType(ServiceType serviceType) {
                Assertions.assertEquals(serviceType, FastjsonConstants.SERVICE_TYPE);
            }

            @Override
            public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
                Assertions.assertEquals(serviceType, FastjsonConstants.SERVICE_TYPE);
            }

            @Override
            public void addAnnotationKey(AnnotationKey annotationKey) {
                Assertions.assertEquals(annotationKey, FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH);
            }
        });
    }
}