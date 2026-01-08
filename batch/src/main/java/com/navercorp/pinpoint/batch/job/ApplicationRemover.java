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
import com.navercorp.pinpoint.batch.vo.CleanTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ApplicationRemover implements ItemWriter<CleanTarget.TypeApplication> {

    private static final Logger logger = LogManager.getLogger(ApplicationRemover.class);

    private final BatchApplicationIndexService batchApplicationIndexService;

    public ApplicationRemover(BatchApplicationIndexService batchApplicationIndexService) {
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "applicationService");
    }

    @Override
    public void write(Chunk<? extends CleanTarget.TypeApplication> targets) throws Exception {
        for (CleanTarget.TypeApplication target : targets) {
            logger.info("Removing application: {}", target);
            try {
                this.batchApplicationIndexService.remove(target.applicationName());
            } catch (Exception e) {
                logger.error("Failed to remove application: {}", target, e);
            }
        }
    }
}
