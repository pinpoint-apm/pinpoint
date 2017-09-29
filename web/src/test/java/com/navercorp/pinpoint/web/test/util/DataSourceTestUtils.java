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

package com.navercorp.pinpoint.web.test.util;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class DataSourceTestUtils {

    public static List<DataSourceBo> createDataSourceBoList(int id, int dataSourceSize, int maxConnectionSize) {
        List<DataSourceBo> result = new ArrayList<>(dataSourceSize);

        for (int i = 0; i < dataSourceSize; i++) {
            DataSourceBo dataSourceBo = createDataSourceBo(id, maxConnectionSize);
            result.add(dataSourceBo);
        }

        return result;
    }

    private static DataSourceBo createDataSourceBo(int id, int maxConnectionSize) {
        DataSourceBo dataSourceBo = new DataSourceBo();
        dataSourceBo.setId(id);
        dataSourceBo.setActiveConnectionSize(RandomUtils.nextInt(1, maxConnectionSize));
        dataSourceBo.setMaxConnectionSize(maxConnectionSize);
        return dataSourceBo;
    }

}
