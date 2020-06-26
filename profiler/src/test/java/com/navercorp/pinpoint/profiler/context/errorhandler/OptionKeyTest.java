package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncContext;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class OptionKeyTest {
    private final Logger logger = LoggerFactory.getLogger(DefaultAsyncContext.class);

    @Test
    public void getkey() {
        String abc = OptionKey.getKey("handlerId", OptionKey.CLASSNAME);
        Assert.assertEquals("profiler.ignore-error-handler.handlerId.class-name", abc);
    }

    @Test
    public void getHandlerId() {
        String abc = OptionKey.getKey("handlerId", OptionKey.CLASSNAME);
        String handlerId = OptionKey.parseHandlerId(abc);
        Assert.assertEquals("handlerId", handlerId);
    }

    @Test
    public void readPattern() {
        Properties properties = new Properties();
        properties.put(OptionKey.getClassName("handler"), "java.lang.RuntimeException");
        ProfilerConfig config = new DefaultProfilerConfig(properties);

        Map<String, String> kv = config.readPattern(OptionKey.PATTERN_REGEX);
        Assert.assertEquals(1, kv.size());
        Assert.assertEquals("java.lang.RuntimeException", kv.get(OptionKey.getClassName("handler")));
    }
}