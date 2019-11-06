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
package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSource;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSourceList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class TFDataSourceListBoMapper {

    private static final TFDataSourceBoMapper tFDataSourceBoMapper = new TFDataSourceBoMapper();

    public TFDataSourceList map(DataSourceListBo dataSourceListBo) {
        List<DataSourceBo> dataSourceBoList = dataSourceListBo.getList();
        List<TFDataSource> dataSourceList = new ArrayList<>(dataSourceBoList.size());

        for (DataSourceBo dataSourceBo : dataSourceBoList) {
            dataSourceList.add(tFDataSourceBoMapper.map(dataSourceBo));
        }

        TFDataSourceList tFDataSourceList = new TFDataSourceList();
        tFDataSourceList.setDataSourceList(dataSourceList);

        return tFDataSourceList;
    }

}
