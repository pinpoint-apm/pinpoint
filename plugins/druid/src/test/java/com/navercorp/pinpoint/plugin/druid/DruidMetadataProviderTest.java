package com.navercorp.pinpoint.plugin.druid;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DruidMetadataProviderTest {

    @Test
    public void test() {

        DruidMetadataProvider provider = new DruidMetadataProvider();

        provider.setup(new TraceMetadataSetupContext() {

            @Override
            public void addServiceType(ServiceType serviceType) {

                Assertions.assertEquals(serviceType, DruidConstants.SERVICE_TYPE);
            }

            @Override
            public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher primaryAnnotationKeyMatcher) {

            }

            @Override
            public void addAnnotationKey(AnnotationKey annotationKey) {

            }
        });
    }
}