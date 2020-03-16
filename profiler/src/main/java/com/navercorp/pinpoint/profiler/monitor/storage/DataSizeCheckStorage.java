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

import com.navercorp.pinpoint.common.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class DataSizeCheckStorage implements RequestUrlStatStorage {

    private final List<String> urlMetadata = new ArrayList<String>();
    private final List<RequestUrlStatInfo> requestUrlStatInfoList = new ArrayList<RequestUrlStatInfo>();

    private final int maxWaterMarkDataSize;

    private int currentDataSize = 0;

    public DataSizeCheckStorage(int maxWaterMarkDataSize) {
        Assert.isTrue(maxWaterMarkDataSize > 0, "'maxWaterMarkDataSize' must be > 0");
        this.maxWaterMarkDataSize = maxWaterMarkDataSize;
    }

    @Override
    public boolean store(RequestUrlStatInfo requestUrlStatInfo) {
        String url = requestUrlStatInfo.getUrl();

        if (!urlMetadata.contains(url)) {
            currentDataSize += url.length() + 4;
        }

        currentDataSize += RequestUrlStatInfo.DEFAULT_DATA_SIZE_WITHOUT_URL;

        requestUrlStatInfoList.add(requestUrlStatInfo);
        return true;
    }


    public boolean isOverDataSize() {
        if (currentDataSize > maxWaterMarkDataSize) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasData() {
        return currentDataSize != 0;
    }

    public List<RequestUrlStatInfo> getAndClear() {
        List<RequestUrlStatInfo> result = new ArrayList<RequestUrlStatInfo>(requestUrlStatInfoList.size());
        result.addAll(requestUrlStatInfoList);

        clear();

        return result;
    }

    private void clear() {
        urlMetadata.clear();
        requestUrlStatInfoList.clear();
        currentDataSize = 0;
    }

}
