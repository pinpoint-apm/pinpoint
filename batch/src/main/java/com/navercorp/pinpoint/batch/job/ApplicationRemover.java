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

import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ApplicationRemover implements ItemWriter<String> {

    private static final Logger logger = LogManager.getLogger(ApplicationRemover.class);

    private final BatchApplicationIndexService batchApplicationIndexService;

    public ApplicationRemover(BatchApplicationIndexService batchApplicationIndexService) {
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "applicationService");
    }

    @Override
    public void write(Chunk<? extends String> applicationNames) throws Exception {
        for (String applicationName : applicationNames) {
            logger.info("Removing application: {}", applicationName);
            this.batchApplicationIndexService.remove(applicationName);
            logger.info("Removed application: {}", applicationName);
        }
    }
}
