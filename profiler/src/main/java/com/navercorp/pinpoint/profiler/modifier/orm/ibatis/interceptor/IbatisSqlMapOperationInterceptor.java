package com.nhn.pinpoint.profiler.modifier.orm.ibatis.interceptor;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.orm.SqlMapOperationInterceptor;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public class IbatisSqlMapOperationInterceptor extends SqlMapOperationInterceptor {

	public IbatisSqlMapOperationInterceptor(ServiceType serviceType) {
		super(serviceType, IbatisSqlMapOperationInterceptor.class);
	}

}
