package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.web.dao.TraceDao;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * TraceDao Factory for compatibility
 * @author Woonduk Kang(emeroad)
 */
@Repository
public class HbaseTraceDaoFactory implements FactoryBean<TraceDao> {

    private final TraceDao v2;

    public HbaseTraceDaoFactory(@Qualifier("hbaseTraceDaoV2") TraceDao v2) {
        this.v2 = Objects.requireNonNull(v2, "v2");
    }

    @Override
    public TraceDao getObject() throws Exception {
        return v2;
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
