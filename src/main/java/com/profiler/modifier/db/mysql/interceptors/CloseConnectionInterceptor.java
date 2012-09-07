package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloseConnectionInterceptor implements StaticBeforeInterceptor {

	private final Logger logger = Logger.getLogger(CloseConnectionInterceptor.class.getName());

	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}
        // close의 경우 호출이 실패하더라도 데이터를 삭제해야함.
		if (target instanceof Connection) {
			ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
			connectionTrace.closeConnection((Connection) target);
		}
	}
}
