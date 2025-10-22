package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.error.ErrorRecorderFactory;
import com.navercorp.pinpoint.profiler.context.error.SimpleErrorRecorderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleErrorRecorderModule extends PrivateModule {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        bind(ErrorRecorderFactory.class).to(SimpleErrorRecorderFactory.class).in(Scopes.SINGLETON);
        expose(ErrorRecorderFactory.class);
    }
}
