package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboTraceMetadataProviderTest {

    @Mock
    TraceMetadataSetupContext context;

    @Test
    public void setup() {
        ApacheDubboTraceMetadataProvider provider = new ApacheDubboTraceMetadataProvider();

        provider.setup(context);

        verify(context).addServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
        verify(context).addServiceType(ApacheDubboConstants.DUBBO_CONSUMER_SERVICE_TYPE);
        verify(context).addServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        verify(context).addAnnotationKey(ApacheDubboConstants.DUBBO_ARGS_ANNOTATION_KEY);
        verify(context).addAnnotationKey(ApacheDubboConstants.DUBBO_RESULT_ANNOTATION_KEY);
        verify(context).addAnnotationKey(ApacheDubboConstants.DUBBO_RPC_ANNOTATION_KEY);
    }
}