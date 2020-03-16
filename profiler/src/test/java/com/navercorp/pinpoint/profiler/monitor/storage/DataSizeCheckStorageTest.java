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

package com.navercorp.pinpoint.profiler.monitor.storage;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class DataSizeCheckStorageTest {

    @Test
    public void overSizeCheckTest() {
        DataSizeCheckStorage storage = new DataSizeCheckStorage(30);

        long startTimeMillis = System.currentTimeMillis();
        RequestUrlStatInfo requestUrlStatInfo1 = new RequestUrlStatInfo("url1", 200, startTimeMillis, startTimeMillis + 10);
        RequestUrlStatInfo requestUrlStatInfo2 = new RequestUrlStatInfo("url2", 200, startTimeMillis, startTimeMillis + 10);

        assertStorage(storage, false, false);

        storage.store(requestUrlStatInfo1);
        assertStorage(storage, false, true);

        storage.store(requestUrlStatInfo2);
        assertStorage(storage, true, true);

        List<RequestUrlStatInfo> result = storage.getAndClear();
        Assert.assertEquals(2, result.size());

        assertStorage(storage, false, false);
    }

    private void assertStorage(DataSizeCheckStorage storage, boolean overDataSize, boolean hasData) {
        Assert.assertEquals(overDataSize, storage.isOverDataSize());
        Assert.assertEquals(hasData, storage.hasData());
    }

}
