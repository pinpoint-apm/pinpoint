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

import java.util.*;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceListBo implements JoinStatBo {

    public static final JoinDataSourceListBo EMPTY_JOIN_DATA_SOURCE_LIST_BO = new JoinDataSourceListBo();

    private List<JoinDataSourceBo> joinDataSourceBoList = Collections.emptyList();
    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    public JoinDataSourceListBo() {
    }

    public JoinDataSourceListBo(String id, List<JoinDataSourceBo> joinDataSourceBoList, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.joinDataSourceBoList = joinDataSourceBoList;
    }

    public List<JoinDataSourceBo> getJoinDataSourceBoList() {
        return joinDataSourceBoList;
    }

    public void setJoinDataSourceBoList(List<JoinDataSourceBo> joinDataSourceBoList) {
        this.joinDataSourceBoList = joinDataSourceBoList;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static JoinDataSourceListBo joinDataSourceListBoList(List<JoinDataSourceListBo> joinDataSourceListBoList, Long timestamp) {
        if (joinDataSourceListBoList.isEmpty()) {
            return EMPTY_JOIN_DATA_SOURCE_LIST_BO;
        }

        JoinDataSourceListBo newJoinDataSourceListBo = new JoinDataSourceListBo();
        JoinDataSourceListBo initJoinDataSourceListBo = joinDataSourceListBoList.get(0);
        newJoinDataSourceListBo.setId(initJoinDataSourceListBo.getId());
        newJoinDataSourceListBo.setTimestamp(timestamp);
        newJoinDataSourceListBo.setJoinDataSourceBoList(joinDatasourceBo(joinDataSourceListBoList));

        return newJoinDataSourceListBo;
    }

    private static List<JoinDataSourceBo> joinDatasourceBo(List<JoinDataSourceListBo> joinDataSourceListBoList) {
        Map<DataSourceKey, List<JoinDataSourceBo>> dataSourceBoListMap = new HashMap<DataSourceKey, List<JoinDataSourceBo>>();

        for (JoinDataSourceListBo joinDataSourceListBo : joinDataSourceListBoList) {
            List<JoinDataSourceBo> dataSourceBoList = joinDataSourceListBo.getJoinDataSourceBoList();

            for (JoinDataSourceBo joinDataSourceBo : dataSourceBoList) {
                DataSourceKey dataSourceKey = new DataSourceKey(joinDataSourceBo.getUrl(), joinDataSourceBo.getServiceTypeCode());
                List<JoinDataSourceBo> joinDataSourceBoList = dataSourceBoListMap.get(dataSourceKey);
                if (joinDataSourceBoList == null) {
                    joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();
                    dataSourceBoListMap.put(dataSourceKey, joinDataSourceBoList);
                }

                joinDataSourceBoList.add(joinDataSourceBo);
            }
        }

        List<JoinDataSourceBo> newJoinDatasourceBoList = new ArrayList<JoinDataSourceBo>();

        for (List<JoinDataSourceBo> joinDataSourceBoList : dataSourceBoListMap.values()) {
            JoinDataSourceBo newJoinDataSourceBo = JoinDataSourceBo.joinDataSourceBoList(joinDataSourceBoList);
            newJoinDatasourceBoList.add(newJoinDataSourceBo);
        }

        return newJoinDatasourceBoList;
    }

    public static class DataSourceKey {
        String url;
        short serviceTypeCode;

        public DataSourceKey(String url, short serviceTypeCode) {
            this.url = url;
            this.serviceTypeCode = serviceTypeCode;
        }

        public String getUrl() {
            return url;
        }

        public short getServiceTypeCode() {
            return serviceTypeCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DataSourceKey that = (DataSourceKey) o;

            if (serviceTypeCode != that.serviceTypeCode) return false;
            return url != null ? url.equals(that.url) : that.url == null;

        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + serviceTypeCode;
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinDataSourceListBo that = (JoinDataSourceListBo) o;

        if (timestamp != that.timestamp) return false;
        if (!joinDataSourceBoList.equals(that.joinDataSourceBoList)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = joinDataSourceBoList.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JoinDataSourceListBo{" +
            "timestamp=" + new Date(timestamp) +
            "joinDataSourceBoList=" + joinDataSourceBoList +
            ", id='" + id + '\'' +
            '}';
    }
}
