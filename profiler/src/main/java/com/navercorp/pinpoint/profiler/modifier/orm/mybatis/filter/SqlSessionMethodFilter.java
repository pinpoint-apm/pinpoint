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

package com.navercorp.pinpoint.profiler.modifier.orm.mybatis.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

/**
 * @author Hyun Jeong
 */
public class SqlSessionMethodFilter implements MethodFilter {
    private static final Set<String> WHITE_LIST_API = createWhiteListApi();

    private static Set<String> createWhiteListApi() {
        return new HashSet<String>(Arrays.asList(
                "selectOne",
                "selectList",
                "selectMap",
                "select",
                "insert",
                "update",
                "delete"
//                "commit",
//                "rollback",
//                "flushStatements",
//                "close",
//                "getConfiguration",
//                "getMapper",
//                "getConnection"
        ));
    }

    @Override
    public boolean accept(MethodInfo ctMethod) {
        if (WHITE_LIST_API.contains(ctMethod.getName())) {
            return ACCEPT;
        }
        return REJECT;
    }

}
