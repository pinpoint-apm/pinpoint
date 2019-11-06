package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.Assert;
import org.junit.Test;

public class FastjsonMetadataProviderTest {

    @Test
    public void setup() {

        FastjsonMetadataProvider provider = new FastjsonMetadataProvider();

        provider.setup(new TraceMetadataSetupContext() {

            @Override
            public void addServiceType(ServiceType serviceType) {
                Assert.assertEquals(serviceType, FastjsonConstants.SERVICE_TYPE);
            }

            @Override
            public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
                Assert.assertEquals(serviceType, FastjsonConstants.SERVICE_TYPE);
            }

            @Override
            public void addAnnotationKey(AnnotationKey annotationKey) {
                Assert.assertEquals(annotationKey, FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH);
            }
        });
    }
}