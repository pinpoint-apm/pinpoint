/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class DataSourceMetricSnapshot {
    private List<DataSource> dataSourceList = new ArrayList<DataSource>();

    public List<DataSource> getDataSourceList() {
        return dataSourceList;
    }

    public void addDataSourceCollectData(DataSource dataSource) {
        this.dataSourceList.add(dataSource);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataSourceMetricSnapshot{");
        sb.append("dataSourceList=").append(dataSourceList);
        sb.append('}');
        return sb.toString();
    }
}
