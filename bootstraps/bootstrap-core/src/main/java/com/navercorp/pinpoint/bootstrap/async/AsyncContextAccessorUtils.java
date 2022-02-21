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

/**
 * @author Woonduk Kang(emeroad)
 */
public final class AsyncContextAccessorUtils {

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
}
