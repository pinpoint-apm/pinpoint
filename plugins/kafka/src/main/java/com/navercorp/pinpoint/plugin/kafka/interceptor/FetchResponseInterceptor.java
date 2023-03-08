package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;

import java.util.Map;
import java.util.Set;

public class FetchResponseInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!(target instanceof EndPointFieldAccessor)) {
            return;
        }

        EndPointFieldAccessor fetchResponse = (EndPointFieldAccessor)target;
        String endpointAddress = fetchResponse._$PINPOINT$_getEndPoint();

        Map<?, ?> responseData = (Map<?, ?>) result;
        Set<?> keySet = responseData.keySet();
        for (Object key : keySet) {
            if (key instanceof EndPointFieldAccessor) {
                EndPointFieldAccessor topicPartition = (EndPointFieldAccessor) key;

                if (topicPartition._$PINPOINT$_getEndPoint() == null) {
                    topicPartition._$PINPOINT$_setEndPoint(endpointAddress);
                }
            }
        }

    }
}
