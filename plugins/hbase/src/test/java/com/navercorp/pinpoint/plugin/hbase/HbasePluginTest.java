package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class HbasePluginTest {

    @Mock
    private ProfilerPluginSetupContext context;

    @Mock
    private TransformTemplate transformTemplate;

    @Test
    public void setup() {
        doReturn(new DefaultProfilerConfig()).when(context).getConfig();
        HbasePlugin plugin = new HbasePlugin();
        plugin.setTransformTemplate(transformTemplate);
        plugin.setup(context);
    }
}