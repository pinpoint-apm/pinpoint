package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.springframework.beans.factory.FactoryBean;


public class SimpleBatchWriterFactoryBean implements FactoryBean<SimpleBatchWriter> {

    private final SimpleBatchWriter batchWriter;

    public SimpleBatchWriterFactoryBean(BufferedMutatorConfiguration configuration,
                                        HbaseBatchWriter hbaseBatchWriter,
                                        HBaseAsyncOperation asyncOperation) {
        if (configuration.isBatchWriter()) {
            this.batchWriter = new SimpleBufferWriter(hbaseBatchWriter);
        } else {
            this.batchWriter =  new AsyncTemplateWriter(asyncOperation);
        }
    }

    @Override
    public SimpleBatchWriter getObject() throws Exception {
        return batchWriter;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleBatchWriter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
