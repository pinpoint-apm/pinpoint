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

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * The type Hbase plugin constants.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public final class HbasePluginConstants {

    private HbasePluginConstants() {
    }

    /**
     * The constant HBASE.
     */
    public static final ServiceType HBASE = ServiceTypeFactory.of(8800, "HBASE", TERMINAL, RECORD_STATISTICS);

    /**
     * The constant HBASE_ADMIN.
     */
    public static final ServiceType HBASE_ADMIN = ServiceTypeFactory.of(8801, "HBASE_ADMIN");

    /**
     * The constant HBASE_TABLE.
     */
    public static final ServiceType HBASE_TABLE = ServiceTypeFactory.of(8802, "HBASE_TABLE");

    /**
     * The constant HBASE_PARAMS.
     */
    public static final AnnotationKey HBASE_PARAMS = AnnotationKeyFactory.of(320, "hbase.params", VIEW_IN_RECORD_SET);

    /**
     * The constant HBASE_SCOPE.
     */
    public static final String HBASE_SCOPE = "HBASE_SCOPE";

    /**
     * The constant HBASE_CONFIG.
     */
    public static final String HBASE_CONFIG = "profiler.hbase.enable";

    /**
     * The constant HBASE_OPS_CONFIG.
     */
    public static final String HBASE_OPS_CONFIG = "profiler.hbase.operation.enable";

}
