package com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter;

import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Hyun Jeong
 */
public final class IbatisInterfaceApi {
	
	private IbatisInterfaceApi() {}

	private static final String[] sqlMapExecutorApis = {
		"insert",
		"delete",
		"update",
		"queryForList",
		"queryForMap",
		"queryForObject",
		"queryForPaginatedList",
		"queryForRowHandler",
		"startBatch",
		"executeBatch",
		"executeBatchDetailed"
	};
	private static final String[] sqlMapTransactionManagerApis = {
		"startTransaction",
		"endTransaction",
		"commitTransaction",
		"getDataSource",
		"getUserConnection",
		"setUserConnection",
		"getCurrentConnection",
	};
	static final String[] sqlMapSessionApis = ArrayUtils.addAll(
		ArrayUtils.addAll(sqlMapExecutorApis, sqlMapTransactionManagerApis), 
		"close"		
	);
	static final String[] sqlMapClientApis = ArrayUtils.addAll(
		sqlMapExecutorApis,
		sqlMapTransactionManagerApis
	);
	
}
