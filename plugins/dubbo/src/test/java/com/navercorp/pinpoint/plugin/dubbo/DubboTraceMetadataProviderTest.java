package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DubboTraceMetadataProviderTest {

    @Mock
    TraceMetadataSetupContext context;

    @Test
    public void setup() {

        DubboTraceMetadataProvider provider = new DubboTraceMetadataProvider();

        provider.setup(context);

        verify(context).addServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
        verify(context).addServiceType(DubboConstants.DUBBO_CONSUMER_SERVICE_TYPE);
        verify(context).addServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        verify(context).addAnnotationKey(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY);
        verify(context).addAnnotationKey(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY);
        verify(context).addAnnotationKey(DubboConstants.DUBBO_RPC_ANNOTATION_KEY);
    }
}