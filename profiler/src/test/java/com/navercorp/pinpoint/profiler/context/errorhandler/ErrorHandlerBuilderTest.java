package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class ErrorHandlerBuilderTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Test
    public void build() {

        Descriptor descriptor = new Descriptor("testHandler", Arrays.asList("java.lang.RuntimeException"), Collections.<String>emptyList(), true);
        ErrorHandlerBuilder errorHandlerBuilder = new ErrorHandlerBuilder(Arrays.asList(descriptor));


    }

   }