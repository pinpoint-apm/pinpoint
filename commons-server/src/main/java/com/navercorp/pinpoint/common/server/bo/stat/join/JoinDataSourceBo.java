/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceBo implements JoinStatBo {
    public static final JoinDataSourceBo EMPTY_JOIN_DATA_SOURCE_BO = new JoinDataSourceBo();
    public static final int UNCOLLECTED_VALUE = -1;
    private static final short UNDEFINED_CATEGORY = -1;
    private static final String EMPTY_URL = "";

    private short serviceTypeCode = UNDEFINED_CATEGORY;
    private String url = EMPTY_URL;
    private int avgActiveConnectionSize = UNCOLLECTED_VALUE;
    private int minActiveConnectionSize = UNCOLLECTED_VALUE;
    private String minActiveConnectionAgentId = UNKNOWN_AGENT;
    private int maxActiveConnectionSize = UNCOLLECTED_VALUE;
    private String maxActiveConnectionAgentId = UNKNOWN_AGENT;

    public JoinDataSourceBo() {
    }

    public JoinDataSourceBo(short serviceTypeCode, String url, int avgActiveConnectionSize, int minActiveConnectionSize, String minActiveConnectionAgentId, int maxActiveConnectionSize, String maxActiveConnectionAgentId) {
        this.serviceTypeCode = serviceTypeCode;
        this.url = url;
        this.avgActiveConnectionSize = avgActiveConnectionSize;
        this.minActiveConnectionSize = minActiveConnectionSize;
        this.minActiveConnectionAgentId = minActiveConnectionAgentId;
        this.maxActiveConnectionSize = maxActiveConnectionSize;
        this.maxActiveConnectionAgentId = maxActiveConnectionAgentId;
    }

    public short getServiceTypeCode() {
        return serviceTypeCode;
    }

    public String getUrl() {
        return url;
    }

    public int getAvgActiveConnectionSize() {
        return avgActiveConnectionSize;
    }

    public int getMinActiveConnectionSize() {
        return minActiveConnectionSize;
    }

    public String getMinActiveConnectionAgentId() {
        return minActiveConnectionAgentId;
    }

    public int getMaxActiveConnectionSize() {
        return maxActiveConnectionSize;
    }

    public String getMaxActiveConnectionAgentId() {
        return maxActiveConnectionAgentId;
    }

    public void setServiceTypeCode(short serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAvgActiveConnectionSize(int avgActiveConnectionSize) {
        this.avgActiveConnectionSize = avgActiveConnectionSize;
    }

    public void setMinActiveConnectionSize(int minActiveConnectionSize) {
        this.minActiveConnectionSize = minActiveConnectionSize;
    }

    public void setMinActiveConnectionAgentId(String minActiveConnectionAgentId) {
        this.minActiveConnectionAgentId = minActiveConnectionAgentId;
    }

    public void setMaxActiveConnectionSize(int maxActiveConnectionSize) {
        this.maxActiveConnectionSize = maxActiveConnectionSize;
    }

    public void setMaxActiveConnectionAgentId(String maxActiveConnectionAgentId) {
        this.maxActiveConnectionAgentId = maxActiveConnectionAgentId;
    }

    @Override
    public long getTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    public static JoinDataSourceBo joinDataSourceBoList(List<JoinDataSourceBo> joinDataSourceBoList) {
        final int boCount = joinDataSourceBoList.size();

        if (boCount == 0) {
            return JoinDataSourceBo.EMPTY_JOIN_DATA_SOURCE_BO;
        }

        final JoinDataSourceBo initJoindataSourceBo = joinDataSourceBoList.get(0);
        int sumActiveConnectionSize = 0;
        int maxActiveConnectionSize = initJoindataSourceBo.getMaxActiveConnectionSize();
        String maxActiveConnectionAgentId = initJoindataSourceBo.getMaxActiveConnectionAgentId();
        int minActiveConnectionSize = initJoindataSourceBo.getMinActiveConnectionSize();
        String minActiveConnectionAgentid = initJoindataSourceBo.getMinActiveConnectionAgentId();

        for (JoinDataSourceBo joinDataSourceBo : joinDataSourceBoList) {
            sumActiveConnectionSize += joinDataSourceBo.getAvgActiveConnectionSize();

            if (joinDataSourceBo.getMaxActiveConnectionSize() > maxActiveConnectionSize) {
                maxActiveConnectionSize = joinDataSourceBo.getMaxActiveConnectionSize();
                maxActiveConnectionAgentId = joinDataSourceBo.getMaxActiveConnectionAgentId();
            }
            if (joinDataSourceBo.getMinActiveConnectionSize() < minActiveConnectionSize) {
                minActiveConnectionSize = joinDataSourceBo.getMinActiveConnectionSize();
                minActiveConnectionAgentid = joinDataSourceBo.getMinActiveConnectionAgentId();
            }
        }

        final JoinDataSourceBo newJoinDataSourceBo = new JoinDataSourceBo();
        newJoinDataSourceBo.setServiceTypeCode(initJoindataSourceBo.getServiceTypeCode());
        newJoinDataSourceBo.setUrl(initJoindataSourceBo.getUrl());
        newJoinDataSourceBo.setAvgActiveConnectionSize(sumActiveConnectionSize / boCount);
        newJoinDataSourceBo.setMinActiveConnectionSize(minActiveConnectionSize);
        newJoinDataSourceBo.setMinActiveConnectionAgentId(minActiveConnectionAgentid);
        newJoinDataSourceBo.setMaxActiveConnectionSize(maxActiveConnectionSize);
        newJoinDataSourceBo.setMaxActiveConnectionAgentId(maxActiveConnectionAgentId);

        return newJoinDataSourceBo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinDataSourceBo that = (JoinDataSourceBo) o;

        if (serviceTypeCode != that.serviceTypeCode) return false;
        if (avgActiveConnectionSize != that.avgActiveConnectionSize) return false;
        if (minActiveConnectionSize != that.minActiveConnectionSize) return false;
        if (maxActiveConnectionSize != that.maxActiveConnectionSize) return false;
        if (!url.equals(that.url)) return false;
        if (!minActiveConnectionAgentId.equals(that.minActiveConnectionAgentId)) return false;
        return maxActiveConnectionAgentId.equals(that.maxActiveConnectionAgentId);

    }

    @Override
    public int hashCode() {
        int result = (int) serviceTypeCode;
        result = 31 * result + url.hashCode();
        result = 31 * result + avgActiveConnectionSize;
        result = 31 * result + minActiveConnectionSize;
        result = 31 * result + minActiveConnectionAgentId.hashCode();
        result = 31 * result + maxActiveConnectionSize;
        result = 31 * result + maxActiveConnectionAgentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JoinDataSourceBo{" +
            "serviceTypeCode=" + serviceTypeCode +
            ", url='" + url + '\'' +
            ", avgActiveConnectionSize=" + avgActiveConnectionSize +
            ", minActiveConnectionSize=" + minActiveConnectionSize +
            ", minActiveConnectionAgentId='" + minActiveConnectionAgentId + '\'' +
            ", maxActiveConnectionSize=" + maxActiveConnectionSize +
            ", maxActiveConnectionAgentId='" + maxActiveConnectionAgentId + '\'' +
            '}';
    }
}
