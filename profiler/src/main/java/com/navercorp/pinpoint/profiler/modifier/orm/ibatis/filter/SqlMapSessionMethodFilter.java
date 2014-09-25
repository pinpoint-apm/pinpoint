package com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Hyun Jeong
 */
public class SqlMapSessionMethodFilter extends IbatisMethodFilter {
	
	private static final Set<String> WHITE_LIST_API = createWhiteListApi();

	private static Set<String> createWhiteListApi() {
		return new HashSet<String>(Arrays.asList(IbatisInterfaceApi.sqlMapSessionApis));
	}
	
	@Override
	protected boolean shouldTrackMethod(String methodName) {
		return WHITE_LIST_API.contains(methodName);
	}

}
