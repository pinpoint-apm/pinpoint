package com.nhn.pinpoint.profiler.modifier.orm.mybatis.interceptor;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.orm.SqlMapOperationInterceptor;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public class MyBatisSqlMapOperationInterceptor extends SqlMapOperationInterceptor {
	
	public MyBatisSqlMapOperationInterceptor(ServiceType serviceType) {
		super(serviceType, MyBatisSqlMapOperationInterceptor.class);
	}

}
