package com.navercorp.pinpoint.plugin.cassandra4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

import java.util.HashMap;
import java.util.Map;

public class SettableByIndexSetInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;

    public SettableByIndexSetInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof BindValueAccessor)) {
            return;
        }

        final BindValueAccessor bindValueAccessor = (BindValueAccessor) target;
        Map<Integer, String> bindMap = bindValueAccessor._$PINPOINT$_getBindValue();
        if (bindMap == null) {
            bindMap = new HashMap<>();
        }

        try {
            final Integer index = ArrayArgumentUtils.getArgument(args, 0, Integer.class);
            if (index == null) {
                return;
            }

            final Object value = ArrayArgumentUtils.getArgument(args, 1, Object.class);
            if (value == null) {
                return;
            }

            final String bind = traceContext.getJdbcContext().getBindVariableService().formatBindVariable(value);
            if (bind != null) {
                bindMap.put(index, bind);
            }
            bindValueAccessor._$PINPOINT$_setBindValue(bindMap);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }
}