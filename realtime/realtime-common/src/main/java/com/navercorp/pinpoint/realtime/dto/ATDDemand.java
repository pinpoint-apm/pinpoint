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
package com.navercorp.pinpoint.realtime.dto;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author youngjin.kim2
 */
public class ATDDemand implements RealtimeDemand {

    private long id;
    private ClusterKey clusterKey;
    private boolean isLight;
    private int limit; // optional
    private @Nullable List<String> threadNameList; // optional
    private @Nullable List<Long> localTraceIdList; // optional

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ClusterKey getClusterKey() {
        return clusterKey;
    }

    public void setClusterKey(ClusterKey clusterKey) {
        this.clusterKey = clusterKey;
    }

    public boolean isLight() {
        return isLight;
    }

    public void setLight(boolean light) {
        isLight = light;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Nullable
    public List<String> getThreadNameList() {
        return threadNameList;
    }

    public void setThreadNameList(@Nullable List<String> threadNameList) {
        this.threadNameList = threadNameList;
    }

    @Nullable
    public List<Long> getLocalTraceIdList() {
        return localTraceIdList;
    }

    public void setLocalTraceIdList(@Nullable List<Long> localTraceIdList) {
        this.localTraceIdList = localTraceIdList;
    }

    @Override
    public String toString() {
        return "ATDDemand{" +
                "id=" + id +
                ", clusterKey=" + clusterKey +
                ", isLight=" + isLight +
                ", limit=" + limit +
                ", threadNameList=" + threadNameList +
                ", localTraceIdList=" + localTraceIdList +
                '}';
    }
}
