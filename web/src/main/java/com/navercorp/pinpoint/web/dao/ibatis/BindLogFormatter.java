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
