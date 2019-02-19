/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPluginTest {
    private ElasticsearchPlugin elasticsearchPlugin = new ElasticsearchPlugin();

    @Test
    public void testSetup() {
        ProfilerPluginSetupContext profilerPluginSetupContext = mock(ProfilerPluginSetupContext.class);
        when(profilerPluginSetupContext.getConfig()).thenReturn(new DefaultProfilerConfig());
        try {
            elasticsearchPlugin.setup(profilerPluginSetupContext);
        }
        catch (Exception e){

        }
    }
    @Test
    public void setTransformTemplate() {
        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        elasticsearchPlugin.setTransformTemplate(new TransformTemplate(instrumentContext));
    }

}
