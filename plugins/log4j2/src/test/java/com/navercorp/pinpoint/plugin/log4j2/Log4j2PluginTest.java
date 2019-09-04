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
import com.navercorp.pinpoint.plugin.log4j2.interceptor.LoggingEventOfLog4j2Interceptor;

import org.apache.logging.log4j.ThreadContext;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author licoco
 * @author King Jin
 */
public class Log4j2PluginTest {

    private Log4j2Plugin plugin = new Log4j2Plugin();

    @Test
    public void setTransformTemplate() {
        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        plugin.setTransformTemplate(new TransformTemplate(instrumentContext));
    }

    @Test
    public void testLoggingEventOfLog4j2Interceptor() {
        TraceContext traceContext = mock(TraceContext.class);
        LoggingEventOfLog4j2Interceptor interceptor = new LoggingEventOfLog4j2Interceptor(traceContext);
        interceptor.before(null);
        interceptor.after(null, null, null);
        Assert.assertNull(ThreadContext.get(LoggingEventOfLog4j2Interceptor.TRANSACTION_ID));
    }

    @Test
    public void testLoggingEventOfLog4j2Interceptor2() {
        TraceContext traceContext = spy(TraceContext.class);
        Trace trace = mock(Trace.class);
        TraceId traceId = spy(TraceId.class);
        String txId = "api_gw01^1565160016090^9878229";
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(traceContext.currentRawTraceObject().getTraceId()).thenReturn(traceId);
        when(traceContext.currentRawTraceObject().getTraceId().getTransactionId()).thenReturn(txId);
        when(traceContext.currentRawTraceObject().getTraceId().getSpanId()).thenReturn(9878229L);

        LoggingEventOfLog4j2Interceptor interceptor = spy(new LoggingEventOfLog4j2Interceptor(traceContext));
        interceptor.before(null);
        interceptor.after(null, null, null);
        Assert.assertEquals(txId, ThreadContext.get(LoggingEventOfLog4j2Interceptor.TRANSACTION_ID));
    }

    @Test
    public void testLog4j2Config() {
        ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        Log4j2Config log4j2Config = new Log4j2Config(profilerConfig);
        Assert.assertTrue(!StringUtils.isEmpty(log4j2Config.toString()));
        Assert.assertTrue(!log4j2Config.isLog4j2LoggingTransactionInfo());
    }

    @Test
    public void testSetup() {

        ProfilerPluginSetupContext profilerPluginSetupContext = spy(ProfilerPluginSetupContext.class);
        DefaultProfilerConfig profilerConfig = spy(new DefaultProfilerConfig());
        when(profilerPluginSetupContext.getConfig()).thenReturn(profilerConfig);
        when(profilerConfig.readBoolean(Log4j2Config.LOG4J2_LOGGING_TRANSACTION_INFO, false)).thenReturn(true);

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
        when(profilerConfig.readBoolean(Log4j2Config.LOG4J2_LOGGING_TRANSACTION_INFO, false)).thenReturn(false);

        Log4j2Config log4j2Config = spy(new Log4j2Config(profilerConfig));
        when(log4j2Config.isLog4j2LoggingTransactionInfo()).thenReturn(true);

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        plugin.setTransformTemplate(new TransformTemplate(instrumentContext));
        plugin.setup(profilerPluginSetupContext);
    }

}
