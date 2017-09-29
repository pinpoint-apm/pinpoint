/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;

/**
 * @author Minwoo Jung
 */
public class ApplicationStatRowKeyComponent {

    private final String applicationId;
    private final StatType statType;
    private final long baseTimestamp;

    public ApplicationStatRowKeyComponent(String applicationId, StatType statType, long baseTimestamp) {
        this.applicationId = applicationId;
        this.statType = statType;
        this.baseTimestamp = baseTimestamp;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public StatType getStatType() {
        return this.statType;
    }

    public long getBaseTimestamp() {
        return this.baseTimestamp;
    }
}
