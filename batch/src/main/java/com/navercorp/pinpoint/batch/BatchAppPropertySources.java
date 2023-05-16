/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@PropertySources({
        @PropertySource(name = "BatchAppPropertySources", value = { BatchAppPropertySources.BATCH_ROOT, BatchAppPropertySources.BATCH_PROFILE}),
        @PropertySource(name = "BatchAppPropertySources-HBase", value = { BatchAppPropertySources.HBASE_ROOT, BatchAppPropertySources.HBASE_PROFILE}),
        @PropertySource(name = "BatchAppPropertySources-JDBC", value = { BatchAppPropertySources.JDBC_ROOT, BatchAppPropertySources.JDBC_PROFILE}),
})
public final class BatchAppPropertySources {
    public static final String HBASE_ROOT= "classpath:hbase-root.properties";
    public static final String HBASE_PROFILE = "classpath:profiles/${pinpoint.profiles.active:release}/hbase.properties";

    public static final String JDBC_ROOT = "classpath:jdbc-root.properties";
    public static final String JDBC_PROFILE = "classpath:profiles/${pinpoint.profiles.active:release}/jdbc.properties";

    public static final String BATCH_ROOT = "classpath:batch-root.properties";
    public static final String BATCH_PROFILE = "classpath:profiles/${pinpoint.profiles.active:release}/batch.properties";

    private BatchAppPropertySources() {
    }
}
