/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.batch.starter;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Where this process reads its settings from.
 *
 * <p>These belong in properties rather than in the packaged {@code application.yml} because
 * of where each lands in the environment: the file named by
 * {@link AlarmBatchStarter#EXTERNAL_CONFIGURATION_KEY} is appended last, which puts it below
 * a packaged {@code application.yml} and above a {@code @PropertySource}. A value shipped in
 * the yaml therefore cannot be changed by the mechanism this deployable offers for changing
 * it -- the operator's file is read and then loses.
 */
@PropertySources({
        @PropertySource(name = "AlarmBatchPropertySources", value = {
                AlarmBatchPropertySources.ALARM_BATCH_ROOT,
                AlarmBatchPropertySources.ALARM_BATCH_PROFILE
        })
})
public final class AlarmBatchPropertySources {

    public static final String ALARM_BATCH_ROOT = "classpath:alarm-batch-root.properties";
    public static final String ALARM_BATCH_PROFILE =
            "classpath:profiles/${pinpoint.profiles.active:release}/alarm-batch.properties";

    private AlarmBatchPropertySources() {
    }
}
