package com.nhn.pinpoint.profiler.modifier.orm.ibatis.interceptor;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.orm.SqlMapOperationInterceptor;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public class IbatisSqlMapOperationInterceptor extends SqlMapOperationInterceptor {

	public IbatisSqlMapOperationInterceptor(ServiceType serviceType) {
		super(serviceType, PLoggerFactory.getLogger(IbatisSqlMapOperationInterceptor.class));
	}

}
