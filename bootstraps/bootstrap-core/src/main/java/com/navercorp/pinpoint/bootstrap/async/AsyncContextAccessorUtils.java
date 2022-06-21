/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.async;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class AsyncContextAccessorUtils {
    private static final AsyncContextCall ASYNC_CONTEXT_CALL = new AsyncContextCall() {
        @Override
        public AsyncContext getAsyncContext(Object object) {
            return AsyncContextAccessorUtils.getAsyncContext(object);
        }

        @Override
        public AsyncContext getAsyncContext(Object[] array, int index) {
            return AsyncContextAccessorUtils.getAsyncContext(array, index);
        }
    };


    public static AsyncContext getAsyncContext(Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return null;
        }
        return getAsyncContext(array[index]);
    }

    public static AsyncContext getAsyncContext(Object object) {
        if (object instanceof AsyncContextAccessor) {
            return ((AsyncContextAccessor) object)._$PINPOINT$_getAsyncContext();
        }
        return null;
    }

    public static void setAsyncContext(final AsyncContext asyncContext, final Object object) {
        if (object instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) object)._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    public static void setAsyncContext(final AsyncContext asyncContext, final Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return;
        }
        setAsyncContext(asyncContext, array[index]);
    }

    public static AsyncContext findAsyncContext(Object[] array, int beginIndex) {
        if (ArrayUtils.isEmpty(array)) {
            return null;
        }
        final int endIndex = array.length - 1;
        return findAsyncContext(array, beginIndex, endIndex);
    }

    public static AsyncContext findAsyncContext(Object[] array, int beginIndex, int endIndex) {
        return findAsyncContext(array, beginIndex, endIndex, ASYNC_CONTEXT_CALL);
    }

    public static AsyncContext findAsyncContext(Object[] array, int beginIndex, int endIndex, AsyncContextCall asyncContextCall) {
        Objects.requireNonNull(asyncContextCall, "asyncContextCall");

        if (ArrayUtils.isEmpty(array)) {
            return null;
        }
        final int length = array.length - 1;
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > length) {
            return null;
        }
        for (int i = beginIndex; i <= endIndex; i++) {
            if (array[i] instanceof Object[]) {
                final Object[] objects = (Object[]) array[i];
                for (Object object : objects) {
                    final AsyncContext asyncContext = asyncContextCall.getAsyncContext(object);
                    if (asyncContext != null) {
                        return asyncContext;
                    }
                }
            } else {
                final AsyncContext asyncContext = asyncContextCall.getAsyncContext(array, i);
                if (asyncContext != null) {
                    return asyncContext;
                }
            }
        }
        return null;
    }
}
