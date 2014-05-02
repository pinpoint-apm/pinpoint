package com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Hyun Jeong
 */
public class SqlMapClientMethodFilter extends IbatisMethodFilter {

	private static final Set<String> WHITE_LIST_API = createWhiteListApi();

	private static final Set<String> createWhiteListApi() {
		return new HashSet<String>(Arrays.asList(IbatisInterfaceApi.sqlMapClientApis));
	}
	
	protected final boolean shouldTrackMethod(String methodName) {
		return WHITE_LIST_API.contains(methodName);
	}
}
