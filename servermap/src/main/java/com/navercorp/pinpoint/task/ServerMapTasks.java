/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.task;

import com.navercorp.pinpoint.servermap.service.ServerMapService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.TimerTask;

/**
 * @author intr3p1d
 */
@Component
public class ServerMapTasks {
    Logger logger = LogManager.getLogger(this.getClass());

    ServerMapService serverMapService;

    public ServerMapTasks(
            ServerMapService serverMapService
    ) {
        this.serverMapService = Objects.requireNonNull(serverMapService, "serverMapService");
    }

    // Update server map data every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void run() {
        logger.info("Updating server map data");
        serverMapService.updateData();
    }

}
