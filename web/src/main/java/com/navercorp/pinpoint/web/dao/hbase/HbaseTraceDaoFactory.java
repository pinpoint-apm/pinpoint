package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.web.dao.TraceDao;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * TraceDao Factory for compatibility
 * @author Woonduk Kang(emeroad)
 */
@Repository
public class HbaseTraceDaoFactory implements FactoryBean<TraceDao> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("hbaseTraceDao")
    private TraceDao v1;

    @Autowired
    @Qualifier("hbaseTraceDaoV2")
    private TraceDao v2;

    @Autowired
    private HBaseAdminTemplate adminTemplate;

    @Value("#{pinpointWebProps['web.span.format.compatibility.version'] ?: 'v2'}")
    private String mode = "v2";

    @Override
    public TraceDao getObject() throws Exception {

        logger.info("TraceDao Compatibility {}", mode);

        final TableName v1TableName = HBaseTables.TRACES;
        final TableName v2TableName = HBaseTables.TRACE_V2;

        if (mode.equalsIgnoreCase("v2")) {
            if (this.adminTemplate.tableExists(v2TableName)) {
                return v2;
            } else {
                logger.error("TraceDao configured for v2, but {} table does not exist", v2TableName);
                throw new IllegalStateException(v2TableName + " table does not exist");
            }
        }
        else if (mode.equalsIgnoreCase("compatibilityMode")) {
            boolean v1TableExists = this.adminTemplate.tableExists(v1TableName);
            boolean v2TableExists = this.adminTemplate.tableExists(v2TableName);
            if (v1TableExists && v2TableExists) {
                return new HbaseTraceCompatibilityDao(v2, v1);
            } else {
                logger.error("TraceDao configured for compatibilityMode, but {} and {} tables do not exist", v1TableName, v2TableName);
                throw new IllegalStateException(v1TableName + ", " + v2TableName + " tables do not exist");
            }
        }
        else if (mode.equalsIgnoreCase("dualRead")) {
            boolean v1TableExists = this.adminTemplate.tableExists(v1TableName);
            boolean v2TableExists = this.adminTemplate.tableExists(v2TableName);
            if (v1TableExists && v2TableExists) {
                return new HbaseDualReadDao(v2, v1);
            } else {
                logger.error("TraceDao configured for dualRead, but {} and {} tables do not exist", v1TableName, v2TableName);
                throw new IllegalStateException(v1TableName + ", " + v2TableName + " tables do not exist");
            }
        }
        else {
            throw new IllegalStateException("Unknown TraceDao configuration : " + mode);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return TraceDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
