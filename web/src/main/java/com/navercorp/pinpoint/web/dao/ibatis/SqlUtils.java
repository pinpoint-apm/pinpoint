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

import java.util.StringTokenizer;

/**
 * query 처리용 util 클래스
 *
 * @author Web Platform Development Lab
 * @author emeroad
 * @since 1.7.4
 */
public final class SqlUtils {
    private SqlUtils() {
    }

    /**
     * query의 빈줄<code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>을 모두 제거하여 한줄로 만든다.
     * @param original 원본 query
     * @return 빈줄이 제거된 query
     */
    public static String removeBreakingWhitespace(String original) {
        StringTokenizer whitespaceStripper = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        while (whitespaceStripper.hasMoreTokens()) {
            builder.append(whitespaceStripper.nextToken());
            builder.append(" ");
        }
        return builder.toString();
    }
}
