package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DataOperationType {
    public static int DISABLE = 0;
    public static int WRITE = 1;
    public static int READ = 2;

    private DataOperationType() {
    }

    public static int resolve(boolean enable, String methodName) {
        if (!enable) {
            return DISABLE;
        }
        if (DataSizeHelper.checkIfWriteOp(methodName)) {
            return WRITE;
        } else if(DataSizeHelper.checkIfReadOp(methodName)) {
            return READ;
        }
        return DISABLE;
    }
}
