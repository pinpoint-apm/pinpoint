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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {

    private final ApplicationContext applicationContext;

    public ConfigManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Async
    @EventListener(SimpleEvent.class)
    public void onRefreshRoute(SimpleEvent event) {
        this.applicationContext.publishEvent(new RefreshRoutesEvent(this));
        System.out.println("REFRESH ROUTE");
    }
}
