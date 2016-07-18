package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
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

    @Value("#{pinpoint_collector_properties['collector.experimental.span.format.compatibility.version'] ?: 'v1'}")
    private String mode = "v1";

    @Override
    public TraceDao getObject() throws Exception {

        logger.info("TraceDao Compatibility {}", mode);

        if (mode.equalsIgnoreCase("v1")) {
            return v1;
        }
        else if (mode.equalsIgnoreCase("v2")) {
            return v2;
        }
        else if(mode.equalsIgnoreCase("dualWrite")) {
            return new DualWriteHbaseTraceDao(v1, v2);
        }

        return v1;
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
