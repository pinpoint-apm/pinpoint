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

public class LoadedClassBo extends AbstractAgentStatDataPoint {
    public static final long UNCOLLECTED_VALUE = UNCOLLECTED_LONG;

    private long loadedClassCount = UNCOLLECTED_VALUE;
    private long unloadedClassCount = UNCOLLECTED_VALUE;

    public LoadedClassBo() {
        super(AgentStatType.LOADED_CLASS);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LoadedClassBo that = (LoadedClassBo) o;

        if (loadedClassCount != that.loadedClassCount) return false;
        return unloadedClassCount == that.unloadedClassCount;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (loadedClassCount ^ (loadedClassCount >>> 32));
        result = 31 * result + (int) (unloadedClassCount ^ (unloadedClassCount >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LoadedClassBo{" +
                "loadedClassCount=" + loadedClassCount +
                ", unloadedClassCount=" + unloadedClassCount +
                "} " + super.toString();
    }
}
