/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.errorhandler;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IgnoreErrorHandlerProvider implements Provider<IgnoreErrorHandler> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProfilerConfig profilerConfig;

    @Inject
    public IgnoreErrorHandlerProvider(ProfilerConfig profilerConfig) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
    }

    @Override
    public IgnoreErrorHandler get() {
        Map<String, String> errorHandlerOption = profilerConfig.readPattern(OptionKey.PATTERN_REGEX);
        logger.info("IgnoreErrorHandler Option:{}", errorHandlerOption);
        DescriptorParser parser = new DescriptorParser(errorHandlerOption);
        List<Descriptor> descriptors = parser.parse();
        logger.info("IgnoreErrorHandler Descriptors:{}", descriptors);
        ErrorHandlerBuilder builder = new ErrorHandlerBuilder(descriptors);
        IgnoreErrorHandler errorHandler = builder.build();
        logger.info("IgnoreErrorHandler:{}", errorHandler);
        return errorHandler;
    }
}
