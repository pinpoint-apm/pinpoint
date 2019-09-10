/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.log4j2.interceptor.LogEventFactoryInterceptor;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author https://github.com/licoco/pinpoint
 */
public class Log4j2PluginTest {


    static final String LOG4J2_LOGGING_TRANSACTION_INFO = "profiler.log4j2.logging.transactioninfo";

    private Log4j2Plugin plugin = new Log4j2Plugin();

    @Test
    public void setTransformTemplate() {
        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        plugin.setTransformTemplate(new TransformTemplate(instrumentContext));
    }

    @Test
    public void testSetup() {
        ProfilerPluginSetupContext profilerPluginSetupContext = spy(ProfilerPluginSetupContext.class);
        DefaultProfilerConfig profilerConfig = spy(new DefaultProfilerConfig());
        when(profilerPluginSetupContext.getConfig()).thenReturn(profilerConfig);
        when(profilerConfig.readBoolean(LOG4J2_LOGGING_TRANSACTION_INFO, false)).thenReturn(true);
        Log4j2Config log4j2Config = spy(new Log4j2Config(profilerConfig));
        when(log4j2Config.isLog4j2LoggingTransactionInfo()).thenReturn(true);

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        plugin.setTransformTemplate(new TransformTemplate(instrumentContext));
        plugin.setup(profilerPluginSetupContext);
    }


    @Test
    public void testSetup2() {
        ProfilerPluginSetupContext profilerPluginSetupContext = spy(ProfilerPluginSetupContext.class);
        DefaultProfilerConfig profilerConfig = spy(new DefaultProfilerConfig());
        when(profilerPluginSetupContext.getConfig()).thenReturn(profilerConfig);
        when(profilerConfig.readBoolean(LOG4J2_LOGGING_TRANSACTION_INFO, false)).thenReturn(false);
        Log4j2Config log4j2Config = spy(new Log4j2Config(profilerConfig));
        when(log4j2Config.isLog4j2LoggingTransactionInfo()).thenReturn(true);

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        plugin.setTransformTemplate(new TransformTemplate(instrumentContext));
        plugin.setup(profilerPluginSetupContext);
    }

}
