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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import java.net.ProtocolException;
import java.util.Date;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadBo implements JoinStatBo {
    public static final JoinCpuLoadBo EMPTY_JOIN_CPU_LOAD_BO = new JoinCpuLoadBo();
    public static final double UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private double jvmCpuLoad = UNCOLLECTED_VALUE;
    private String maxJvmCpuAgentId = UNKNOWN_AGENT;
    private double maxJvmCpuLoad = UNCOLLECTED_VALUE;
    private String minJvmCpuAgentId = UNKNOWN_AGENT;
    private double minJvmCpuLoad = UNCOLLECTED_VALUE;

    private double systemCpuLoad = UNCOLLECTED_VALUE;
    private String maxSysCpuAgentId = UNKNOWN_AGENT;
    private double maxSystemCpuLoad = UNCOLLECTED_VALUE;
    private String minSysCpuAgentId = UNKNOWN_AGENT;
    private double minSystemCpuLoad = UNCOLLECTED_VALUE;

    public JoinCpuLoadBo() {
    }

    public JoinCpuLoadBo(String id, double jvmCpuLoad, double maxJvmCpuLoad, String maxJvmCpuAgentId, double minJvmCpuLoad, String minJvmCpuAgentId, double systemCpuLoad, double maxSystemCpuLoad, String maxSysCpuAgentId, double minSystemCpuLoad, String minSysCpuAgentId, long timestamp) {
        this.id = id;
        this.jvmCpuLoad = jvmCpuLoad;
        this.minJvmCpuLoad = minJvmCpuLoad;
        this.minJvmCpuAgentId = minJvmCpuAgentId;
        this.maxJvmCpuLoad = maxJvmCpuLoad;
        this.maxJvmCpuAgentId = maxJvmCpuAgentId;
        this.systemCpuLoad = systemCpuLoad;
        this.minSystemCpuLoad = minSystemCpuLoad;
        this.minSysCpuAgentId = minSysCpuAgentId;
        this.maxSystemCpuLoad = maxSystemCpuLoad;
        this.maxSysCpuAgentId = maxSysCpuAgentId;
        this.timestamp = timestamp;
    }

    public static JoinCpuLoadBo joinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList, Long timestamp) {
        int boCount = joinCpuLoadBoList.size();

        if (joinCpuLoadBoList.size() == 0) {
            return EMPTY_JOIN_CPU_LOAD_BO;
        }

        JoinCpuLoadBo newJoinCpuLoadBo = new JoinCpuLoadBo();
        JoinCpuLoadBo initCpuLoadBo = joinCpuLoadBoList.get(0);
        newJoinCpuLoadBo.setId(initCpuLoadBo.getId());
        newJoinCpuLoadBo.setTimestamp(timestamp);

        double sumJvmCpuLoad = 0D;
        String maxJvmCpuAgentId = initCpuLoadBo.getMaxJvmCpuAgentId();
        double maxJvmCpuLoad = initCpuLoadBo.getMaxJvmCpuLoad();
        String minJvmCpuAgentId = initCpuLoadBo.getMinJvmCpuAgentId();
        double minJvmCpuLoad = initCpuLoadBo.getMinJvmCpuLoad();
        double sumSystemCpuLoad = 0D;
        String maxSysCpuAgentId = initCpuLoadBo.getMaxSysCpuAgentId();
        double maxSystemCpuLoad = initCpuLoadBo.getMaxSystemCpuLoad();
        String minSysCpuAgentId = initCpuLoadBo.getMinSysCpuAgentId();
        double minSystemCpuLoad = initCpuLoadBo.getMinSystemCpuLoad();

        for (JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {
            sumJvmCpuLoad += joinCpuLoadBo.getJvmCpuLoad();
            if (joinCpuLoadBo.getMaxJvmCpuLoad() > maxJvmCpuLoad) {
                maxJvmCpuLoad = joinCpuLoadBo.getMaxJvmCpuLoad();
                maxJvmCpuAgentId = joinCpuLoadBo.getMaxJvmCpuAgentId();
            }
            if (joinCpuLoadBo.getMinJvmCpuLoad() < minJvmCpuLoad) {
                minJvmCpuLoad = joinCpuLoadBo.getMinJvmCpuLoad();
                minJvmCpuAgentId = joinCpuLoadBo.getMinJvmCpuAgentId();
            }

            sumSystemCpuLoad += joinCpuLoadBo.getSystemCpuLoad();
            if (joinCpuLoadBo.getMaxSystemCpuLoad() > maxSystemCpuLoad) {
                maxSystemCpuLoad = joinCpuLoadBo.getMaxSystemCpuLoad();
                maxSysCpuAgentId = joinCpuLoadBo.getMaxSysCpuAgentId();
            }
            if (joinCpuLoadBo.getMinSystemCpuLoad() < minSystemCpuLoad) {
                minSystemCpuLoad = joinCpuLoadBo.getMinSystemCpuLoad();
                minSysCpuAgentId = joinCpuLoadBo.getMinSysCpuAgentId();
            }
        }

        newJoinCpuLoadBo.setJvmCpuLoad(sumJvmCpuLoad / (double) boCount);
        newJoinCpuLoadBo.setMaxJvmCpuLoad(maxJvmCpuLoad);
        newJoinCpuLoadBo.setMaxJvmCpuAgentId(maxJvmCpuAgentId);
        newJoinCpuLoadBo.setMinJvmCpuLoad(minJvmCpuLoad);
        newJoinCpuLoadBo.setMinJvmCpuAgentId(minJvmCpuAgentId);
        newJoinCpuLoadBo.setSystemCpuLoad(sumSystemCpuLoad / (double) boCount);
        newJoinCpuLoadBo.setMinSystemCpuLoad(minSystemCpuLoad);
        newJoinCpuLoadBo.setMinSysCpuAgentId(minSysCpuAgentId);
        newJoinCpuLoadBo.setMaxSystemCpuLoad(maxSystemCpuLoad);
        newJoinCpuLoadBo.setMaxSysCpuAgentId(maxSysCpuAgentId);

        return newJoinCpuLoadBo;
    }

    public String getMaxJvmCpuAgentId() {
        return maxJvmCpuAgentId;
    }

    public String getMinJvmCpuAgentId() {
        return minJvmCpuAgentId;
    }

    public String getMaxSysCpuAgentId() {
        return maxSysCpuAgentId;
    }

    public String getMinSysCpuAgentId() {
        return minSysCpuAgentId;
    }

    public void setMaxJvmCpuAgentId(String maxJvmCpuAgentId) {
        this.maxJvmCpuAgentId = maxJvmCpuAgentId;
    }

    public void setMinJvmCpuAgentId(String minJvmCpuAgentId) {
        this.minJvmCpuAgentId = minJvmCpuAgentId;
    }

    public void setMaxSysCpuAgentId(String maxSysCpuAgentId) {
        this.maxSysCpuAgentId = maxSysCpuAgentId;
    }

    public void setMinSysCpuAgentId(String minSysCpuAgentId) {
        this.minSysCpuAgentId = minSysCpuAgentId;
    }

    public void setJvmCpuLoad(double jvmCpuLoad) {
        this.jvmCpuLoad = jvmCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getJvmCpuLoad() {
        return jvmCpuLoad;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public String getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMaxJvmCpuLoad(double maxJvmCpuLoad) {
        this.maxJvmCpuLoad = maxJvmCpuLoad;
    }

    public void setMinJvmCpuLoad(double minJvmCpuLoad) {
        this.minJvmCpuLoad = minJvmCpuLoad;
    }

    public void setMaxSystemCpuLoad(double maxSystemCpuLoad) {
        this.maxSystemCpuLoad = maxSystemCpuLoad;
    }

    public void setMinSystemCpuLoad(double minSystemCpuLoad) {
        this.minSystemCpuLoad = minSystemCpuLoad;
    }

    public double getMaxJvmCpuLoad() {
        return maxJvmCpuLoad;
    }

    public double getMinJvmCpuLoad() {
        return minJvmCpuLoad;
    }

    public double getMaxSystemCpuLoad() {
        return maxSystemCpuLoad;
    }

    public double getMinSystemCpuLoad() {
        return minSystemCpuLoad;
    }

    @Override
    public String toString() {
        return "JoinCpuLoadBo{" +
            "id='" + id + '\'' +
            ", jvmCpuLoad=" + jvmCpuLoad +
            ", maxJvmCpuAgentId='" + maxJvmCpuAgentId + '\'' +
            ", maxJvmCpuLoad=" + maxJvmCpuLoad +
            ", minJvmCpuAgentId='" + minJvmCpuAgentId + '\'' +
            ", minJvmCpuLoad=" + minJvmCpuLoad +
            ", systemCpuLoad=" + systemCpuLoad +
            ", maxSysCpuAgentId='" + maxSysCpuAgentId + '\'' +
            ", maxSystemCpuLoad=" + maxSystemCpuLoad +
            ", minSysCpuAgentId='" + minSysCpuAgentId + '\'' +
            ", minSystemCpuLoad=" + minSystemCpuLoad +
            ", timestamp=" + timestamp +"(" + new Date(timestamp)+ ")" +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinCpuLoadBo that = (JoinCpuLoadBo) o;

        if (Double.compare(that.jvmCpuLoad, jvmCpuLoad) != 0) return false;
        if (Double.compare(that.maxJvmCpuLoad, maxJvmCpuLoad) != 0) return false;
        if (Double.compare(that.minJvmCpuLoad, minJvmCpuLoad) != 0) return false;
        if (Double.compare(that.systemCpuLoad, systemCpuLoad) != 0) return false;
        if (Double.compare(that.maxSystemCpuLoad, maxSystemCpuLoad) != 0) return false;
        if (Double.compare(that.minSystemCpuLoad, minSystemCpuLoad) != 0) return false;
        if (timestamp != that.timestamp) return false;
        if (!id.equals(that.id)) return false;
        if (!maxJvmCpuAgentId.equals(that.maxJvmCpuAgentId)) return false;
        if (!minJvmCpuAgentId.equals(that.minJvmCpuAgentId)) return false;
        if (!maxSysCpuAgentId.equals(that.maxSysCpuAgentId)) return false;
        return minSysCpuAgentId.equals(that.minSysCpuAgentId);
    }
}
