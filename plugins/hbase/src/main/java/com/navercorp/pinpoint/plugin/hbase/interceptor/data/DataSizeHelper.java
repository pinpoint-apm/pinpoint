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

/**
 * @author jimo
 **/
public class DataSizeHelper {

    private static final String LIST = "java.util.List";

    private DataSizeHelper() {
    }

    public static boolean checkIfMutationOp(String methodName) {
        return HbasePluginConstants.mutationMethodNames.contains(methodName);
    }

    public static boolean checkIfRowMutationOp(String methodName) {
        return HbasePluginConstants.rowMutationMethodNames.contains(methodName);
    }

    public static boolean checkIfGetResultOp(String methodName) {
        return HbasePluginConstants.getResultMethodNames.contains(methodName);
    }

    public static boolean parameterTypesIncludesList(String[] parameterTypes) {
        if (parameterTypes.length <= 0) {
            return false;
        }
        String lastParameter = parameterTypes[parameterTypes.length - 1];
        return lastParameter.contains(LIST);
    }

    /**
     * Calculate the last arg data size of write method.
     */
    public static int getDataSizeFromArgument(Object[] args, int dataOperationType) {
        DataSizeProvider dataSizeProvider = DataSizeProviderFactory.getDataSizeProvider(dataOperationType);
        Object arg = args[args.length - 1];
        return dataSizeProvider.getDataSize(arg);
    }

    /**
     * Calculate the result data size of read method
     */
    public static int getDataSizeFromResult(Object result, int dataOperationType) {
        DataSizeProvider dataSizeProvider = DataSizeProviderFactory.getDataSizeProvider(dataOperationType);
        return dataSizeProvider.getDataSize(result);
    }
}