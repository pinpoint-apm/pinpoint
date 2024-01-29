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
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author youngjin.kim2
 */
public class ApplicationReader implements ItemStreamReader<String> {

    private static final Logger logger = LogManager.getLogger(ApplicationReader.class);
    private static final String CURRENT_INDEX = "current.index";

    private final BatchApplicationService applicationService;

    private List<String> applicationNames;

    private final AtomicInteger currentIndexAtom = new AtomicInteger(0);

    public ApplicationReader(BatchApplicationService applicationService) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
    }

    @Override
    public String read() {
        int currentIndex = currentIndexAtom.getAndIncrement();
        if (currentIndex < applicationNames.size()) {
            logger.info("Reading application: {} / {}", currentIndex, applicationNames.size());
            return applicationNames.get(currentIndex);
        } else {
            return null;
        }
    }

    @Override
    public void open(@Nonnull ExecutionContext executionContext) throws ItemStreamException {
        logger.info("Opened application reader");
        this.applicationNames = getAllApplications();
        logger.info("Application reader has {} applications", applicationNames.size());
        if (executionContext.containsKey(CURRENT_INDEX)) {
            int loadedIndex = executionContext.getInt(CURRENT_INDEX);
            this.currentIndexAtom.set(loadedIndex);
            logger.info("Application reader starts from index {}", loadedIndex);
        } else {
            this.currentIndexAtom.set(0);
            logger.info("Application reader starts from beginning");
        }
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_INDEX, this.currentIndexAtom.get());
    }

    @Override
    public void close() throws ItemStreamException {
        logger.info("Closing application reader");
    }

    private List<String> getAllApplications() {
        return this.applicationService.getAll()
                .stream()
                .sorted()
                .toList();
    }

}
