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

import com.navercorp.pinpoint.batch.service.BatchApplicationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class ApplicationRemover implements ItemWriter<String> {

    private static final Logger logger = LogManager.getLogger(ApplicationRemover.class);

    private final BatchApplicationService applicationService;

    public ApplicationRemover(BatchApplicationService applicationService) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
    }

    @Override
    public void write(Chunk<? extends String> applicationIds) throws Exception {
        for (String applicationIdStr : applicationIds) {
            UUID applicationId = UUID.fromString(applicationIdStr);
            logger.info("Removing application: {}", applicationId);
            this.applicationService.remove(applicationId);
            logger.info("Removed application: {}", applicationId);
        }
    }
}
