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

package com.navercorp.pinpoint.web.vo;

/**
 * 
 * @author netspider
 * @author emeroad
 * @param <V>
 */
public class LimitedScanResult<V> {

    private long limitedTime;
    private V data;

    public V getScanData() {
        return data;
    }

    public void setScanData(V scanData) {
        this.data = scanData;
    }

    public long getLimitedTime() {
        return limitedTime;
    }

    public void setLimitedTime(long limitedTime) {
        this.limitedTime = limitedTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LimitedScanResult{");
        sb.append("limitedTime=").append(limitedTime);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
