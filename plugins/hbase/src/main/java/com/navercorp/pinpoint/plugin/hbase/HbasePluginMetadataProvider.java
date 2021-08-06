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
package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * The type Hbase plugin metadata provider.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbasePluginMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(HbasePluginConstants.HBASE_CLIENT);
        context.addServiceType(HbasePluginConstants.HBASE_CLIENT_ADMIN);
        context.addServiceType(HbasePluginConstants.HBASE_CLIENT_TABLE);
        context.addServiceType(HbasePluginConstants.HBASE_ASYNC_CLIENT);
        context.addAnnotationKey(HbasePluginConstants.HBASE_CLIENT_PARAMS);
        context.addAnnotationKey(HbasePluginConstants.HBASE_TABLE_NAME);
    }
}
