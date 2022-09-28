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

import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;

import java.util.Arrays;
import java.util.List;

/**
 * @author jimo
 **/
public class DataSizeHelper {

    private static final List<? extends WriteSizeProvider> WRITE_SIZE_PROVIDERS = Arrays.asList(
            new MutateSizeProvider(),
            new PutListSizeProvider(),
            new DeleteListSizeProvider(),
            new RowMutationSizeProvider()
    );

    private static final List<? extends ReadSizeProvider> READ_SIZE_PROVIDERS = Arrays.asList(
            new GetSizeProvider(),
            new GetListSizeProvider()
    );

    private DataSizeHelper() {
    }

    /**
     * Divide HTable methods into write and read method,
     * get approximated data size from the method and the given information.
     */
    public static int getDataSizeFrom(String methodName, Object[] args, Object result) {
        try {
            if (args != null && args.length > 0 && checkIfWriteOp(methodName)) {
                return getDataWriteSize(args);
            }
            if (result != null && checkIfReadOp(methodName)) {
                return getDataReadSize(result);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return 0;
    }

    private static boolean checkIfWriteOp(String methodName) {
        return HbasePluginConstants.tableWriteMethodNames.contains(methodName);
    }

    private static boolean checkIfReadOp(String methodName) {
        return HbasePluginConstants.tableReadMethodNames.contains(methodName);
    }

    /*
     * Calculate the last arg data size of write method.
     */
    private static int getDataWriteSize(Object[] args) {
        Object arg = args[args.length - 1];
        for (WriteSizeProvider w : WRITE_SIZE_PROVIDERS) {
            if (w.isProviderOf(arg)) {
                return w.getDataSize(arg);
            }
        }
        return 0;
    }

    /*
     * Calculate the result data size of read method
     */
    private static int getDataReadSize(Object result) {
        for (ReadSizeProvider r : READ_SIZE_PROVIDERS) {
            if (r.isProviderOf(result)) {
                return r.getDataSize(result);
            }
        }
        return 0;
    }
}