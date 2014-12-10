package com.nhncorp.lucy.spring.db.mybatis.plugin.util;

import com.nhncorp.lucy.spring.db.mybatis.plugin.BindLogFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Date;
import java.util.List;
import java.util.Properties;

/**
 * {@link org.apache.ibatis.mapping.BoundSql} 을 중간에 가로체 {@link org.apache.ibatis.mapping.BoundSql#getSql()} 의 동작을 변경하기 위한 proxy
 *
 * @author Web Platform Development Lab
 * @author emeroad
 * @since 1.7.4
 */
public class DefaultBindingLogFormatter implements BindLogFormatter {

    private final Log logger = LogFactory.getLog(this.getClass());
    private boolean removeWhitespace = true;

    public void setRemoveWhitespace(boolean removeWhitespace) {
        this.removeWhitespace = removeWhitespace;
    }

    public String format(String query, List<String> parameters) {
        if (isEmpty(query)) {
            return query;
        }
        if (removeWhitespace) {
            query = SqlUtils.removeBreakingWhitespace(query);
        }
        StringBuilder builder = new StringBuilder(query.length() << 1);

        int index = 0;
        int queryPrev = 0;

        for (int i = 0; i < query.length(); ) {
            if (parameters != null && query.charAt(i) == '?') {
                builder.append(query.substring(queryPrev, i));
                builder.append(parameters.size() > index ? convert(parameters.get(index)) : null);
                queryPrev = ++i;
                index++;
                continue;
            }
            i++;
        }
        if (queryPrev < query.length()) {
            builder.append(query.substring(queryPrev));
        }

        return builder.toString();
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        String removeWhitespace = properties.getProperty("removeWhitespace");
        if (removeWhitespace != null) {
            if (logger.isInfoEnabled()) {
                logger.info("DefaultBindingLogFormatter removeWhitespace:" + removeWhitespace);
            }
            this.removeWhitespace = Boolean.parseBoolean(removeWhitespace);
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }


    /**
     * Convert.
     *
     * @param paramter the paramter
     * @return the string
     */
    private String convert(Object paramter) {
        if (paramter instanceof String || paramter instanceof Date) {
            return "'" + paramter + "'";
        }

        return String.valueOf(paramter);
    }
}
