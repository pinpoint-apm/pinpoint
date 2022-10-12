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

import java.util.Arrays;
import java.util.List;

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
     * The constant HBASE_CLIENT.
     */
    public static final ServiceType HBASE_CLIENT = ServiceTypeFactory.of(8800, "HBASE_CLIENT", TERMINAL, RECORD_STATISTICS);

    /**
     * The constant HBASE_CLIENT_ADMIN.
     */
    public static final ServiceType HBASE_CLIENT_ADMIN = ServiceTypeFactory.of(8801, "HBASE_CLIENT_ADMIN");

    /**
     * The constant HBASE_CLIENT_TABLE.
     */
    public static final ServiceType HBASE_CLIENT_TABLE = ServiceTypeFactory.of(8802, "HBASE_CLIENT_TABLE");

    /**
     * The constant HBASE_ASYNC_CLIENT.
     */
    public static final ServiceType HBASE_ASYNC_CLIENT = ServiceTypeFactory.of(8803, "HBASE_ASYNC_CLIENT");

    /**
     * The constant HBASE_CLIENT_PARAMS.
     */
    public static final AnnotationKey HBASE_CLIENT_PARAMS = AnnotationKeyFactory.of(320, "hbase.client.params", VIEW_IN_RECORD_SET);

    /**
     * The constant HBASE_TABLE_NAME.
     */
    public static final AnnotationKey HBASE_TABLE_NAME = AnnotationKeyFactory.of(321, "hbase.table.name", VIEW_IN_RECORD_SET);

    /**
     * The data size of param and result constant
     */
    public static final AnnotationKey HBASE_OP_READ_SIZE = AnnotationKeyFactory.of(325, "hbase.op.read.size", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HBASE_OP_WRITE_SIZE = AnnotationKeyFactory.of(326, "hbase.op.write.size", VIEW_IN_RECORD_SET);

    /**
     * each rpc result number of scan
     */
    public static final AnnotationKey HBASE_SCAN_RPC_RESULT_NUM = AnnotationKeyFactory.of(327, "hbase.scan.rpc.result.num", VIEW_IN_RECORD_SET);

    /**
     * The constant HBASE_DESTINATION_ID.
     */
    public static final String HBASE_DESTINATION_ID = "HBASE";

    /**
     * The constant HBASE_CLIENT_SCOPE.
     */
    public static final String HBASE_CLIENT_SCOPE = "HBASE_CLIENT_SCOPE";

    /**
     * The constant HBASE_CLIENT_CONFIG.
     */
    public static final String HBASE_CLIENT_CONFIG = "profiler.hbase.client.enable";

    /**
     * The constant HBASE_CLIENT_ADMIN_CONFIG.
     */
    public static final String HBASE_CLIENT_ADMIN_CONFIG = "profiler.hbase.client.admin.enable";

    /**
     * The constant HBASE_CLIENT_TABLE_CONFIG.
     */
    public static final String HBASE_CLIENT_TABLE_CONFIG = "profiler.hbase.client.table.enable";

    /**
     * The constant HBASE_CLIENT_PARAMS_CONFIG.
     */
    public static final String HBASE_CLIENT_PARAMS_CONFIG = "profiler.hbase.client.params.enable";

    /**
     * The constant HBASE_CLIENT_TABLENAME_CONFIG.
     */
    public static final String HBASE_CLIENT_TABLENAME_CONFIG = "profiler.hbase.client.tablename.enable";

    /**
     * The constant HBASE_CLIENT_DATA_SIZE_CONFIG
     */
    public static final String HBASE_CLIENT_DATA_SIZE_CONFIG = "profiler.hbase.client.data-size.enable";

    /**
     * The constant tableMethodNames.
     */

    protected static final String[] tableMethodNames = new String[]{"append", "increment", "exists", "existsAll", "get", "getScanner", "put", "checkAndPut", "delete", "checkAndDelete", "mutateRow", "checkAndMutate"};

    /**
     * exists, existsAll are skipped because it returns only boolean. (not related to data size)
     * getScanner is skipped. (not related to data size)
     */
    public static final List<String> mutationMethodNames = Arrays.asList("append", "increment", "put", "checkAndPut", "delete", "checkAndDelete", "checkAndMutate");
    public static final List<String> rowMutationMethodNames = Arrays.asList("mutateRow");
    public static final List<String> getResultMethodNames = Arrays.asList("get");

    /**
     * The constant adminMethodNames.
     */
    protected static final String[] adminMethodNames = new String[]{"tableExists", "listTables", "listTableNames", "getTableDescriptor", "createTable", "deleteTable", "modifyTable", "truncateTable", "enableTable", "enableTableAsync", "enableTables", "disableTableAsync", "disableTable", "disableTables", "getAlterStatus", "addColumn", "deleteColumn", "modifyColumn", "compact", "majorCompact", "split", "getTableRegions", "snapshot", "restoreSnapshot", "cloneSnapshot", "listSnapshots", "deleteSnapshot"};

    public static final String UNKNOWN_TABLE = "Unknown";
}
