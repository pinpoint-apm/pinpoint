package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.web.dao.TraceDao;
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


    @Value("#{pinpointWebProps['web.experimental.span.format.compatibility.version'] ?: 'v1'}")
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
        else if (mode.equalsIgnoreCase("compatibilityMode")) {
            return new HbaseTraceCompatibilityDao(v2, v1);
        }
        else if (mode.equalsIgnoreCase("dualRead")) {
            return new HbaseDualReadDao(v2, v1);
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
