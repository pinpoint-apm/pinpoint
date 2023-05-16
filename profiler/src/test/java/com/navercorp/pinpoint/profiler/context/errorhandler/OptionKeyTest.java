package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionKeyTest {
    private final Logger logger = LogManager.getLogger(DefaultAsyncContext.class);

    @Test
    public void getKey() {
        String abc = OptionKey.getKey("handlerId", OptionKey.CLASSNAME);
        assertEquals("profiler.ignore-error-handler.handlerId.class-name", abc);
    }

    @Test
    public void getHandlerId() {
        String abc = OptionKey.getKey("handlerId", OptionKey.CLASSNAME);
        String handlerId = OptionKey.parseHandlerId(abc);
        assertEquals("handlerId", handlerId);
    }

    @Test
    public void readPattern() {
        Properties properties = new Properties();
        properties.put(OptionKey.getClassName("handler"), "java.lang.RuntimeException");
        ProfilerConfig config = ProfilerConfigLoader.load(properties);

        Map<String, String> kv = config.readPattern(OptionKey.PATTERN_REGEX);
        assertThat(kv)
                .hasSize(1)
                .containsEntry(OptionKey.getClassName("handler"), "java.lang.RuntimeException");
    }
}