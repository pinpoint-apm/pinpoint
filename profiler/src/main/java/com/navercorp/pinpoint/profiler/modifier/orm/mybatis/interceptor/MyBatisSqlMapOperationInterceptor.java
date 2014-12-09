package com.navercorp.pinpoint.profiler.modifier.orm.mybatis.interceptor;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.orm.SqlMapOperationInterceptor;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public class MyBatisSqlMapOperationInterceptor extends SqlMapOperationInterceptor {
	
	public MyBatisSqlMapOperationInterceptor(ServiceType serviceType) {
		super(serviceType, MyBatisSqlMapOperationInterceptor.class);
	}

}
