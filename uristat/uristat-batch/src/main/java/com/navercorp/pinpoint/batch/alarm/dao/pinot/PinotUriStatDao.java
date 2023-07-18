package com.navercorp.pinpoint.batch.alarm.dao.pinot;

import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.batch.alarm.vo.UriStatQueryParams;
import com.navercorp.pinpoint.batch.alarm.dao.UriStatDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotUriStatDao implements UriStatDao {
    private static final String NAMESPACE = UriStatDao.class.getName() + ".";


    private final SqlSessionTemplate sqlSessionTemplate;

    public PinotUriStatDao(@Qualifier("uriStatSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public double selectTotalCount(UriStatQueryParams params) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectTotalCount", params);
    }

    @Override
    public double selectFailureCount(UriStatQueryParams params) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectFailureCount", params);
    }
    @Override
    public double selectApdex(UriStatQueryParams params) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectApdex", params);
    }

    @Override
    public double selectAvgResponse(UriStatQueryParams params) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectAvgResponse", params);
    }

    @Override
    public double selectMaxResponse(UriStatQueryParams params) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectMaxResponse", params);
    }

    @Override
    public boolean checkIfKeyExists(UriStatQueryParams params) {
        double count = sqlSessionTemplate.selectOne(NAMESPACE + "checkIfKeyExists", params);
        return (count > 0);
    }
}
