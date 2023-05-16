/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.common.server.starter.BasicStarter;

public class BatchStarter extends BasicStarter {
    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "BatchExternalEnvironment";
    public static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.batch.config.location";

    public BatchStarter(Class<?>... sources) {
        super(sources);
        this.externalPropertySourceName = EXTERNAL_PROPERTY_SOURCE_NAME;
        this.externalConfigurationKey = EXTERNAL_CONFIGURATION_KEY;
    }
}
