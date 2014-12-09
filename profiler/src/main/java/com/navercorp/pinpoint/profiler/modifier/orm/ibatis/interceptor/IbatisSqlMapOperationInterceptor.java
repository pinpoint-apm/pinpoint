package com.navercorp.pinpoint.profiler.modifier.orm.ibatis.interceptor;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.orm.SqlMapOperationInterceptor;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public class IbatisSqlMapOperationInterceptor extends SqlMapOperationInterceptor {

	public IbatisSqlMapOperationInterceptor(ServiceType serviceType) {
		super(serviceType, IbatisSqlMapOperationInterceptor.class);
	}

}
