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
package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import com.navercorp.pinpoint.plugin.hbase.interceptor.data.DataOperationType;
import com.navercorp.pinpoint.plugin.hbase.interceptor.data.DataSizeHelper;
import com.navercorp.pinpoint.plugin.hbase.interceptor.util.HbaseTableNameProvider;
import com.navercorp.pinpoint.plugin.hbase.interceptor.util.HbaseTableNameProviderFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.List;

/**
 * The type Hbase table method interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbaseTableMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean paramsProfile;
    private final boolean tableNameProfile;
    private final int dataOpType;
    private final HbaseTableNameProvider nameProvider;

    /**
     * Instantiates a new Hbase table method interceptor.
     *
     * @param traceContext  the trace context
     * @param descriptor    the descriptor
     * @param paramsProfile params
     */
    public HbaseTableMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor,
                                       boolean paramsProfile, boolean tableNameProfile, int hbaseVersion, int dataOpType) {
        super(traceContext, descriptor);
        this.paramsProfile = paramsProfile;
        this.tableNameProfile = tableNameProfile;
        this.nameProvider = HbaseTableNameProviderFactory.getTableNameProvider(hbaseVersion);
        this.dataOpType = dataOpType;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(HbasePluginConstants.HBASE_CLIENT_TABLE);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (paramsProfile) {
            String attributes = parseAttributes(args);
            if (attributes != null) {
                recorder.recordAttribute(HbasePluginConstants.HBASE_CLIENT_PARAMS, attributes);
            }
        }
        if (tableNameProfile) {
            String tableName = getTableName(target);
            recorder.recordAttribute(HbasePluginConstants.HBASE_TABLE_NAME, tableName);
        }

        if (DataOperationType.DISABLE == dataOpType) {
            // skip
        } else if (DataOperationType.isWriteOp(dataOpType)) {
            int dataWriteSize = DataSizeHelper.getDataSizeFromArgument(args, dataOpType);
            recorder.recordAttribute(HbasePluginConstants.HBASE_OP_WRITE_SIZE, dataWriteSize);
        } else if (DataOperationType.isReadOp(dataOpType)) {
            int dataReadSize = DataSizeHelper.getDataSizeFromResult(result, dataOpType);
            recorder.recordAttribute(HbasePluginConstants.HBASE_OP_READ_SIZE, dataReadSize);
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordException(throwable);
    }

    private String getTableName(Object target) {
        try {
            return nameProvider.getName(target);
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("failed to getTableName method. caused:{}", e.getMessage(), e);
            }
        }
        return HbasePluginConstants.UNKNOWN_TABLE;
    }

    /**
     * Parse attributes string.
     *
     * @param args the args
     * @return the string
     */
    protected String parseAttributes(Object[] args) {

        Object param;
        final int argsLength = ArrayUtils.getLength(args);
        if (argsLength >= 1) {
            param = args[argsLength - 1];
        } else {
            return null;
        }

        // Put/Delete/Append/Increment
        if (param instanceof Mutation) {
            Mutation mutation = (Mutation) param;
            return "rowKey: " + Bytes.toStringBinary(mutation.getRow());
        }
        if (param instanceof Get) {
            Get get = (Get) param;
            return "rowKey: " + Bytes.toStringBinary(get.getRow());
        }
        if (param instanceof Scan) {
            Scan scan = (Scan) param;
            String startRowKey = Bytes.toStringBinary(scan.getStartRow());
            String stopRowKey = Bytes.toStringBinary(scan.getStopRow());
            return "startRowKey: " + startRowKey + " stopRowKey: " + stopRowKey;
        }
        // if param instanceof List.
        if (param instanceof List) {
            List<?> list = (List<?>) param;
            return "size: " + list.size();
        }
        return null;
    }
}
