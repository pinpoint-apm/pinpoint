package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FastjsonPluginTest {

    private FastjsonPlugin plugin = new FastjsonPlugin();

    @Test
    public void setTransformTemplate() {
        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        plugin.setTransformTemplate(new TransformTemplate(instrumentContext));
    }

    @Test
    public void setup() {
        ProfilerPluginSetupContext profilerPluginSetupContext = mock(ProfilerPluginSetupContext.class);
        when(profilerPluginSetupContext.getConfig()).thenReturn(new DefaultProfilerConfig());

        plugin.setup(profilerPluginSetupContext);
    }
}