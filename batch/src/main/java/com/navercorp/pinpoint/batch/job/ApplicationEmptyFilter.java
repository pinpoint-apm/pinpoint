/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.service.ApplicationService;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ApplicationEmptyFilter implements ItemProcessor<String, String> {

    private static final Logger logger = LogManager.getLogger(ApplicationEmptyFilter.class);

    private final ApplicationService applicationService;
    private final Duration emptyDurationThreshold;

    public ApplicationEmptyFilter(ApplicationService applicationService, Duration emptyDurationThreshold) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.emptyDurationThreshold = Objects.requireNonNull(emptyDurationThreshold, "emptyDurationThreshold");
    }

    @Override
    public String process(@Nonnull String s) throws Exception {
        if (isApplicationEmpty(s)) {
            logger.info("Application is empty: {}", s);
            return s;
        } else {
            logger.info("Application is not empty: {}", s);
            return null;
        }
    }

    private boolean isApplicationEmpty(String applicationName) {
        return this.applicationService.isApplicationEmpty(applicationName, this.emptyDurationThreshold);
    }
}
