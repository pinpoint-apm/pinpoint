/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

public class LoadedClassBo extends AbstractStatDataPoint {

    private long loadedClassCount;
    private long unloadedClassCount;

    public LoadedClassBo(DataPoint point,
                         long loadedClassCount, long unloadedClassCount) {
        super(point);
        this.loadedClassCount = loadedClassCount;
        this.unloadedClassCount = unloadedClassCount;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.LOADED_CLASS;
    }

    public long getLoadedClassCount() {
        return loadedClassCount;
    }

    public void setLoadedClassCount(long loadedClassCount) {
        this.loadedClassCount = loadedClassCount;
    }

    public long getUnloadedClassCount() {
        return unloadedClassCount;
    }

    public void setUnloadedClassCount(long unloadedClassCount) {
        this.unloadedClassCount = unloadedClassCount;
    }


    @Override
    public String toString() {
        return "LoadedClassBo{" +
                "point=" + point +
                ", loadedClassCount=" + loadedClassCount +
                ", unloadedClassCount=" + unloadedClassCount +
                '}';
    }
}
