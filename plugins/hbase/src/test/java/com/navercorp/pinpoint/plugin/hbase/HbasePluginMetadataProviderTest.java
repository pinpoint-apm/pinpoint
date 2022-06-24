package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HbasePluginMetadataProviderTest {

    @Mock
    private TraceMetadataSetupContext context;

    @Test
    public void setup() {

        HbasePluginMetadataProvider provider = new HbasePluginMetadataProvider();
        provider.setup(context);

        verify(context).addServiceType(HbasePluginConstants.HBASE_CLIENT);
        verify(context).addServiceType(HbasePluginConstants.HBASE_CLIENT_ADMIN);
        verify(context).addServiceType(HbasePluginConstants.HBASE_CLIENT_TABLE);
        verify(context).addServiceType(HbasePluginConstants.HBASE_ASYNC_CLIENT);
        verify(context).addAnnotationKey(HbasePluginConstants.HBASE_CLIENT_PARAMS);
    }
}