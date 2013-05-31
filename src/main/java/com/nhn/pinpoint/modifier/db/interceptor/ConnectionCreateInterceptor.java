package com.nhn.pinpoint.modifier.db.interceptor;

import java.sql.Connection;
import java.util.Arrays;

import com.nhn.pinpoint.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.logging.Logger;

import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.util.InterceptorUtils;
import com.nhn.pinpoint.util.MetaObject;
import com.nhn.pinpoint.util.StringUtils;

@Deprecated
public class ConnectionCreateInterceptor implements StaticAroundInterceptor {

    private final Logger logger = LoggerFactory.getLogger(ConnectionCreateInterceptor.class.getName());
    private final MetaObject setUrl = new MetaObject("__setUrl", String.class);

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        // TODO 생성 시간 측정시 아래 코드를 다시 생각해야 됨.
        if (!InterceptorUtils.isSuccess(result)) {
            return;
        }
        // TODO before도 같이 후킹하여 Connection 생성시간도 측정해야 됨.
        // datasource의 pool을 고려할것.
        if (result instanceof Connection) {
            Object url = args[4];
            if (url instanceof String) {
                this.setUrl.invoke(result, url);
            }
        }
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {

    }
}
