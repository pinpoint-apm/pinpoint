/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.alarm.collector;

import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;

/**
 * @author minwoo.jung
 */
public abstract class DataCollector {
    
    protected final DataCollectorCategory dataCollectorCategory;
    
    public DataCollector(DataCollectorCategory dataCollectorCategory) {
        this.dataCollectorCategory = dataCollectorCategory;
    }
    
    public abstract void collect();
    
    public DataCollectorCategory getDataCollectorCategory() {
        return dataCollectorCategory;
    }

    protected long calculatePercent(long used, long total) {
        if (total == 0 || used == 0) {
            return 0;
        } else {
            return (used * 100L) / total;
        }
    }

}
