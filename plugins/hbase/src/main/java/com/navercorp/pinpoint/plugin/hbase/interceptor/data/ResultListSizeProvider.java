/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

import org.apache.hadoop.hbase.client.Result;

/**
 * Result[] HTable.get(List<Get>)
 *
 * @author jimo
 **/
public class ResultListSizeProvider implements DataSizeProvider {
    @Override
    public int getDataSize(Object param) {
        Result[] results = (Result[]) param;
        int sizeInByte = 0;
        for (Result result : results) {
            sizeInByte += DataSizeUtils.sizeOfResult(result);
        }
        return sizeInByte;
    }
}
