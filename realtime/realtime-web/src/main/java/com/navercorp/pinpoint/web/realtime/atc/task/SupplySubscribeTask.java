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

import com.navercorp.pinpoint.web.realtime.atc.service.SupplySubscribeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class SupplySubscribeTask extends PeriodicTimerTask {

    private static final Logger logger = LogManager.getLogger(SupplySubscribeTask.class);

    private final Object lock = new Object();

    private Set<String> prevTopics = Set.of();

    private final SupplySubscribeService service;

    public SupplySubscribeTask(SupplySubscribeService service) {
        super(logger, "SupplySubscribe", 500);
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    protected void runPeriodicTask() {
        sync();
    }

    public void sync() {
        synchronized (lock) {
            this.prevTopics = service.updateSubscriptions(this.prevTopics);
        }
    }

}
