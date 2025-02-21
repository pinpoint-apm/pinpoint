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

import com.navercorp.pinpoint.common.util.ArrayUtils;

public final class ReactorSubscriberAccessorUtils {

    public static ReactorSubscriber get(Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return null;
        }
        return get(array[index]);
    }

    public static ReactorSubscriber get(Object object) {
        if (object instanceof ReactorSubscriberAccessor) {
            return ((ReactorSubscriberAccessor) object)._$PINPOINT$_getReactorSubscriber();
        }
        return null;
    }

    public static void set(final ReactorSubscriber subscriber, final Object object) {
        if (object instanceof ReactorSubscriberAccessor) {
            ((ReactorSubscriberAccessor) object)._$PINPOINT$_setReactorSubscriber(subscriber);
        }
    }

    public static void set(final ReactorSubscriber subscriber, final Object[] array, int index) {
        if (!ArrayUtils.isArrayIndexValid(array, index)) {
            return;
        }
        set(subscriber, array[index]);
    }

    public static ReactorSubscriber find(Object[] array, int beginIndex) {
        if (ArrayUtils.isEmpty(array)) {
            return null;
        }
        final int endIndex = array.length - 1;
        return find(array, beginIndex, endIndex);
    }

    public static ReactorSubscriber find(Object[] array, int beginIndex, int endIndex) {
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
                    final ReactorSubscriber subscriber = get(object);
                    if (subscriber != null) {
                        return subscriber;
                    }
                }
            } else {
                final ReactorSubscriber subscriber = get(array, i);
                if (subscriber != null) {
                    return subscriber;
                }
            }
        }
        return null;
    }
}