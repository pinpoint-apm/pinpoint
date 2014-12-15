package com.navercorp.pinpoint.web.dao.ibatis;

import java.util.List;
import java.util.Properties;

/**
 * Bind Log의 format결정하는 interface
 *
 * @author Web Platform Development Lab
 * @author emeroad
 * @see com.navercorp.pinpoint.web.dao.ibatis.DefaultBindingLogFormatter
 * @since 1.7.4
 */
public interface BindLogFormatter {

    /**
     * query에 bind 변수를 치환위한 method
     * @param query 원본 query
     * @param parameters 바인딩 변수
     * @return 원본 query에 바인딩 변수를 치환한 query
     */
    String format(String query, List<String> parameters);

    /**
     * 옵션 설정용 properties
     * @param properties 옵션 properties
     */
    void setProperties(Properties properties);
}
