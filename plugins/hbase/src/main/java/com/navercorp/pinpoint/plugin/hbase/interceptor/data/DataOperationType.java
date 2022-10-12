package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DataOperationType {
    public static final int DISABLE = 0;
    public static final int MUTATION = 1;
    public static final int MUTATION_LIST = 2;
    public static final int ROW_MUTATION = 3;
    public static final int RESULT = 4;
    public static final int RESULT_LIST = 5;


    private DataOperationType() {
    }

    public static int resolve(boolean enable, String methodName, String[] parameterTypes) {
        if (!enable) {
            return DISABLE;
        }
        if (DataSizeHelper.checkIfMutationOp(methodName)) {
            if (DataSizeHelper.parameterTypesIncludesList(parameterTypes)) {
                return MUTATION_LIST;
            }
            return MUTATION;
        } else if (DataSizeHelper.checkIfRowMutationOp(methodName)) {
            return ROW_MUTATION;
        } else if (DataSizeHelper.checkIfGetResultOp(methodName)) {
            if (DataSizeHelper.parameterTypesIncludesList(parameterTypes)) {
                return RESULT_LIST;
            }
            return RESULT;
        }
        return DISABLE;
    }

    public static boolean isWriteOp(int dataOperationType) {
        return dataOperationType == MUTATION || dataOperationType == MUTATION_LIST || dataOperationType == ROW_MUTATION;
    }

    public static boolean isReadOp(int dataOperationType) {
        return dataOperationType == RESULT || dataOperationType == RESULT_LIST;
    }
}
