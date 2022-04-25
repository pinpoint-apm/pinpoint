/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextCall;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;

public final class ReactorContextAccessorUtils {

    private static final AsyncContextCall REACTOR_CONTEXT_CALL = new AsyncContextCall() {
        @Override
        public AsyncContext getAsyncContext(Object object) {
            return ReactorContextAccessorUtils.getAsyncContext(object);
        }

        @Override
        public AsyncContext getAsyncContext(Object[] array, int index) {
            return ReactorContextAccessorUtils.getAsyncContext(array, index);
        }
    };


    public static AsyncContext getAsyncContext(Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return null;
        }
        return getAsyncContext(array[index]);
    }

    public static AsyncContext getAsyncContext(Object object) {
        if (object instanceof ReactorContextAccessor) {
            return ((ReactorContextAccessor) object)._$PINPOINT$_getReactorContext();
        }
        return null;
    }

    public static void setAsyncContext(final AsyncContext asyncContext, final Object object) {
        if (object instanceof ReactorContextAccessor) {
            final AsyncContext argAsyncContext = ReactorContextAccessorUtils.getAsyncContext(object);
            if (argAsyncContext == null) {
                ((ReactorContextAccessor) object)._$PINPOINT$_setReactorContext(asyncContext);
            }
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
        return AsyncContextAccessorUtils.findAsyncContext(array, beginIndex, endIndex, REACTOR_CONTEXT_CALL);
    }
}
