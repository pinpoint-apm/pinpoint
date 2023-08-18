/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.uristat.web.chart;

import com.navercorp.pinpoint.uristat.web.dao.UriStatChartDao;

import java.util.List;
import java.util.Objects;

public class DefaultUriStatChartType implements UriStatChartType {

    private final String type;

    private final List<String> fieldNames;
    private final UriStatChartDao chartDao;

    public DefaultUriStatChartType(String type, List<String> fieldNames, UriStatChartDao chartDao) {
        this.type = Objects.requireNonNull(type, "type");
        this.fieldNames = Objects.requireNonNull(fieldNames, "fieldNames");
        this.chartDao = Objects.requireNonNull(chartDao, "chartDao");
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public UriStatChartDao getChartDao() {
        return chartDao;
    }

    @Override
    public String toString() {
        return "UriStatChartType{" +
                "type='" + type + '\'' +
                ", fieldNames=" + fieldNames +
                ", chartDao=" + chartDao.getClass().getSimpleName() +
                '}';
    }
}
