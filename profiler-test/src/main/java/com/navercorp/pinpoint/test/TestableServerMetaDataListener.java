/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder.ServerMetaDataListener;

/**
 * @author HyunGil Jeong
 */
public class TestableServerMetaDataListener implements ServerMetaDataListener {
    
    private volatile ServerMetaData serverMetaData;

    @Override
    public void publishServerMetaData(ServerMetaData serverMetaData) {
        this.serverMetaData = serverMetaData;
    }
    
    public ServerMetaData getServerMetaData() {
        return this.serverMetaData;
    }
    
}
