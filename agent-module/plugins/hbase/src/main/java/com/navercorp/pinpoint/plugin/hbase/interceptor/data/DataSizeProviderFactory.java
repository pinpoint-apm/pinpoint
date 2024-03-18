package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

public class DataSizeProviderFactory {

    private DataSizeProviderFactory() {
    }

    public static DataSizeProvider getDataSizeProvider(int dataOperationType) {
        switch (dataOperationType) {
            case DataOperationType.MUTATION:
                return new MutationSizeProvider();
            case DataOperationType.MUTATION_LIST:
                return new MutationListSizeProvider();
            case DataOperationType.ROW_MUTATION:
                return new RowMutationSizeProvider();
            case DataOperationType.RESULT:
                return new ResultSizeProvider();
            case DataOperationType.RESULT_LIST:
                return new ResultListSizeProvider();
            default:
                return new UnsupportedSizeProvider();
        }
    }

}
