/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.ibatis;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Date;
import java.util.List;
import java.util.Properties;

/**
 * proxy for intercepting {@link org.apache.ibatis.mapping.BoundSql} and changing operations of {@link org.apache.ibatis.mapping.BoundSql#getSql()}

 * @author emeroad
 */
public class DefaultBindingLogFormatter implements BindLogFormatter {

    private final Log logger = LogFactory.getLog(this.getClass());
    private boolean removeWhitespace = true;

    public void setRemoveWhitespace(boolean removeWhitespace) {
        this.removeWhitespace = removeWhitespace;
    }

    public String format(String query, List<String> parameters) {
        if (StringUtils.isEmpty(query)) {
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



    /**
     * Convert.
     *
     * @param parameter the parameter
     * @return the string
     */
    private String convert(Object parameter) {
        if (parameter instanceof String || parameter instanceof Date) {
            return "'" + parameter + "'";
        }

        return String.valueOf(parameter);
    }
}
