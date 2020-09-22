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
import java.util.Objects;
import java.util.stream.Collectors;

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
    private JoinIntFieldBo activeConnectionSizeJoinValue = JoinIntFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinDataSourceBo() {
    }

    public JoinDataSourceBo(short serviceTypeCode, String url, int avgActiveConnectionSize, int minActiveConnectionSize, String minActiveConnectionAgentId, int maxActiveConnectionSize, String maxActiveConnectionAgentId) {
        this(serviceTypeCode, url, new JoinIntFieldBo(avgActiveConnectionSize, minActiveConnectionSize, minActiveConnectionAgentId, maxActiveConnectionSize, maxActiveConnectionAgentId));
    }

    public JoinDataSourceBo(short serviceTypeCode, String url, JoinIntFieldBo activeConnectionSizeJoinValue) {
        this.serviceTypeCode = serviceTypeCode;
        this.url = url;
        this.activeConnectionSizeJoinValue = Objects.requireNonNull(activeConnectionSizeJoinValue, "activeConnectionSizeJoinValue");
    }

    public short getServiceTypeCode() {
        return serviceTypeCode;
    }

    public String getUrl() {
        return url;
    }

    public JoinIntFieldBo getActiveConnectionSizeJoinValue() {
        return activeConnectionSizeJoinValue;
    }

    public void setServiceTypeCode(short serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setActiveConnectionSizeJoinValue(JoinIntFieldBo activeConnectionSizeJoinValue) {
        this.activeConnectionSizeJoinValue = activeConnectionSizeJoinValue;
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

        List<JoinIntFieldBo> activeConnectionSizeFieldBoList = joinDataSourceBoList.stream().map(JoinDataSourceBo::getActiveConnectionSizeJoinValue).collect(Collectors.toList());
        JoinIntFieldBo activeConnectionSizeJoinValue = JoinIntFieldBo.merge(activeConnectionSizeFieldBoList);

        final JoinDataSourceBo firstJoindataSourceBo = joinDataSourceBoList.get(0);

        final JoinDataSourceBo newJoinDataSourceBo = new JoinDataSourceBo();
        newJoinDataSourceBo.setServiceTypeCode(firstJoindataSourceBo.getServiceTypeCode());
        newJoinDataSourceBo.setUrl(firstJoindataSourceBo.getUrl());
        newJoinDataSourceBo.setActiveConnectionSizeJoinValue(activeConnectionSizeJoinValue);
        return newJoinDataSourceBo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinDataSourceBo that = (JoinDataSourceBo) o;

        if (serviceTypeCode != that.serviceTypeCode) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return activeConnectionSizeJoinValue != null ? activeConnectionSizeJoinValue.equals(that.activeConnectionSizeJoinValue) : that.activeConnectionSizeJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = (int) serviceTypeCode;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (activeConnectionSizeJoinValue != null ? activeConnectionSizeJoinValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinDataSourceBo{");
        sb.append("serviceTypeCode=").append(serviceTypeCode);
        sb.append(", url='").append(url).append('\'');
        sb.append(", activeConnectionSizeJoinValue=").append(activeConnectionSizeJoinValue);
        sb.append('}');
        return sb.toString();
    }

}
