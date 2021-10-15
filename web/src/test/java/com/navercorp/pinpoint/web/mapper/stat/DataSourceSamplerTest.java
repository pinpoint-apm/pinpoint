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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DataSourceSampler;
import com.navercorp.pinpoint.web.test.util.DataSourceTestUtils;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class DataSourceSamplerTest {

    private static final int MIN_VALUE_OF_MAX_CONNECTION_SIZE = 20;
    private static final int CREATE_TEST_OBJECT_MAX_SIZE = 10;

    private final DataSourceSampler sampler = new DataSourceSampler();

    @Test
    public void sampleDataPointsTest1() {
        int testObjectSize = RandomUtils.nextInt(1, CREATE_TEST_OBJECT_MAX_SIZE);
        int maxConnectionSize = RandomUtils.nextInt(MIN_VALUE_OF_MAX_CONNECTION_SIZE, MIN_VALUE_OF_MAX_CONNECTION_SIZE * 2);
        List<DataSourceBo> dataSourceBoList = DataSourceTestUtils.createDataSourceBoList(1, testObjectSize, maxConnectionSize);

        SampledDataSource sampledDataSource = sampler.sampleDataPoints(0, System.currentTimeMillis(), dataSourceBoList, null);

        assertEquals(sampledDataSource, dataSourceBoList);
    }

    private void assertEquals(SampledDataSource sampledDataSource, List<DataSourceBo> dataSourceBoList) {
        int minActiveConnectionSize = Integer.MAX_VALUE;
        int maxActiveConnectionSize = Integer.MIN_VALUE;
        int sumActiveConnectionSize = 0;

        int minMaxConnectionSize = Integer.MAX_VALUE;
        int maxMaxConnectionSize = Integer.MIN_VALUE;
        int sumMaxConnectionSize = 0;

        for (DataSourceBo dataSourceBo : dataSourceBoList) {
            int activeConnectionSize = dataSourceBo.getActiveConnectionSize();
            minActiveConnectionSize = Math.min(activeConnectionSize, minActiveConnectionSize);
            maxActiveConnectionSize = Math.max(activeConnectionSize, maxActiveConnectionSize);
            sumActiveConnectionSize += activeConnectionSize;

            int maxConnectionSize = dataSourceBo.getMaxConnectionSize();
            minMaxConnectionSize = Math.min(maxConnectionSize, minMaxConnectionSize);
            maxMaxConnectionSize = Math.max(maxConnectionSize, maxMaxConnectionSize);
            sumMaxConnectionSize += maxConnectionSize;
        }

        Assert.assertEquals((int) sampledDataSource.getActiveConnectionSize().getMinYVal(), minActiveConnectionSize);
        Assert.assertEquals((int) sampledDataSource.getActiveConnectionSize().getMaxYVal(), maxActiveConnectionSize);
        Assert.assertEquals((int) sampledDataSource.getActiveConnectionSize().getSumYVal(), sumActiveConnectionSize);

        Assert.assertEquals((int) sampledDataSource.getMaxConnectionSize().getMinYVal(), minMaxConnectionSize);
        Assert.assertEquals((int) sampledDataSource.getMaxConnectionSize().getMaxYVal(), maxMaxConnectionSize);
        Assert.assertEquals((int) sampledDataSource.getMaxConnectionSize().getSumYVal(), sumMaxConnectionSize);
    }

}
