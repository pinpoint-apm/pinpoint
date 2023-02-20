/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.realtime.atc.task;

import com.navercorp.pinpoint.web.realtime.atc.service.DemandPublishService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class DemandPublishTask extends PeriodicTimerTask {

    private static final Logger logger = LogManager.getLogger(DemandPublishTask.class);

    private final DemandPublishService service;

    public DemandPublishTask(long periodMs, DemandPublishService service) {
        super(logger, "DemandPublish", periodMs);
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    public void runPeriodicTask() {
        this.service.demand();
    }

}
