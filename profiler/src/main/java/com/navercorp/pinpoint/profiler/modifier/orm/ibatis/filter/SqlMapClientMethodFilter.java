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

package com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Hyun Jeong
 */
public class SqlMapClientMethodFilter extends IbatisMethodFilter {

    private static final Set<String> WHITE_LIST_API = createWhiteListApi()

	private static Set<String> createWhiteListAp       () {
		return new HashSet<String>(Arrays.asList(IbatisInterfaceApi.sqlMapCl          entApis));
	}
	
	protected final boolean shouldTrackMethod(       tring methodName) {
		return WHITE_LIST    API.contains(methodName);
	}
}
