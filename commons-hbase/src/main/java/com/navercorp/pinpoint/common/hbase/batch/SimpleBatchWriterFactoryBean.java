package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.SimpleBatchWriter;
import org.springframework.beans.factory.FactoryBean;


public class SimpleBatchWriterFactoryBean implements FactoryBean<SimpleBatchWriter> {

    private final SimpleBatchWriter batchWriter;

    public SimpleBatchWriterFactoryBean(BufferedMutatorConfiguration configuration,
                                        HbaseBatchWriter hbaseBatchWriter,
                                        HbaseTemplate2 HbaseTemplate2) {
        if (configuration != null && configuration.isBatchWriter()) {
            this.batchWriter = new SimpleBufferWriter(hbaseBatchWriter);
        } else {
            this.batchWriter = new HbaseTemplateWriter(HbaseTemplate2);
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
