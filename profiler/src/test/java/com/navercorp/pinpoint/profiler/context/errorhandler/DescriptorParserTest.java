package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DescriptorParserTest {

    @Test
    public void parse() {
        Map<String, String> map = new HashMap<>();
        map.put(OptionKey.getClassName("custom-error-handler"), "java.lang.RuntimeException");
        map.put(OptionKey.getExceptionMessageContains("custom-error-handler"), "error");

        DescriptorParser parser = new DescriptorParser(map);
        List<Descriptor> descriptorList = parser.parse();
        Assertions.assertEquals(descriptorList.size(), 1);

        ErrorHandlerBuilder builder = new ErrorHandlerBuilder(descriptorList);
        IgnoreErrorHandler errorHandler = builder.build();

        Assertions.assertTrue(errorHandler.handleError(new RuntimeException(" error ")));

        Assertions.assertFalse(errorHandler.handleError(new RuntimeException(" success")));
        Assertions.assertFalse(errorHandler.handleError(new SQLException(" success")));
        Assertions.assertFalse(errorHandler.handleError(new SQLException(" error")));
    }

    @Test
    public void parse_message_regex_test() {
        String errorHandlerId = "custom-error-handler";

        Properties properties = new Properties();
        properties.put(OptionKey.getClassName(errorHandlerId), "java.lang.RuntimeException");
        properties.put(OptionKey.getExceptionMessageContains(errorHandlerId), "test");
        ProfilerConfig config = ProfilerConfigLoader.load(properties);

        Map<String, String> errorHandlerProperties = config.readPattern(OptionKey.PATTERN_REGEX);

        Assertions.assertEquals(errorHandlerProperties.get(OptionKey.getClassName(errorHandlerId)), "java.lang.RuntimeException");
        Assertions.assertEquals(errorHandlerProperties.get(OptionKey.getExceptionMessageContains(errorHandlerId)), "test");

    }
}