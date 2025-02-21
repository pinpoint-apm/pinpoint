/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

public class CoreSubscriberOnSubscribeInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public CoreSubscriberOnSubscribeInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            // args[0]
            final ReactorSubscriber subscriptionReactorSubscriber = getSubscriptionReactorSubscriber(args);
            // actual
            final ReactorSubscriber actualReactorSubscriber = getActualReactorSubscriber(target);
            // target
            ReactorSubscriber thisReactorSubscriber = getThisReactorSubscriber(target);

            // set this
            if (thisReactorSubscriber == null) {
                // e.g. actual.onSubscribe(this);
                thisReactorSubscriber = passSubscriptionToThis(subscriptionReactorSubscriber);
            }

            if (thisReactorSubscriber == null) {
                // Fill in the missing part of the subscriptionOrReturn
                thisReactorSubscriber = passActualToThis(actualReactorSubscriber);
            }

            if (thisReactorSubscriber == null) {
                // not found reactorSubscriber
                return;
            }

            // set subscription
            if (subscriptionReactorSubscriber == null) {
                // TODO need to check
                passThisToSubscription(thisReactorSubscriber, args);
            }

            // set actual
            if (actualReactorSubscriber == null) {
                passThisToActual(thisReactorSubscriber, target);
            }

            onSubscribe(thisReactorSubscriber, target);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }


    private ReactorSubscriber getThisReactorSubscriber(Object target) {
        final ReactorSubscriber thisReactorSubscriber = ReactorSubscriberAccessorUtils.get(target);
        if (thisReactorSubscriber != null) {
            if (isDebug) {
                logger.debug("this reactorSubscriber={}", thisReactorSubscriber);
            }
        }
        return thisReactorSubscriber;
    }

    private ReactorSubscriber getSubscriptionReactorSubscriber(Object[] args) {
        final ReactorSubscriber subscriptionReactorSubscriber = ReactorSubscriberAccessorUtils.get(args, 0);
        if (subscriptionReactorSubscriber != null) {
            if (isDebug) {
                logger.debug("subscription(args[0]) reactorSubscriber={}", subscriptionReactorSubscriber);
            }
        }
        return subscriptionReactorSubscriber;
    }

    private ReactorSubscriber getActualReactorSubscriber(Object target) {
        if (target instanceof ReactorActualAccessor) {
            final ReactorSubscriberAccessor reactorSubscriberAccessor = ((ReactorActualAccessor) target)._$PINPOINT$_getReactorActual();
            if (reactorSubscriberAccessor != null) {
                final ReactorSubscriber reactorSubscriber = reactorSubscriberAccessor._$PINPOINT$_getReactorSubscriber();
                if (reactorSubscriber != null) {
                    if (isDebug) {
                        logger.debug("actual(parent) reactorSubscriber={}", reactorSubscriber);
                    }
                    return reactorSubscriber;
                }
            }
        }
        return null;
    }

    private ReactorSubscriber passSubscriptionToThis(ReactorSubscriber reactorSubscriber) {
        if (reactorSubscriber != null) {
            if (isDebug) {
                logger.debug("Pass subscription(args[0]) to this");
            }
            return new ReactorSubscriber(reactorSubscriber.getAsyncContext());
        }

        return null;
    }

    private ReactorSubscriber passActualToThis(ReactorSubscriber reactorSubscriber) {
        if (reactorSubscriber != null) {
            if (isDebug) {
                logger.debug("Pass actual(parent) to this");
            }
            return new ReactorSubscriber(reactorSubscriber.getAsyncContext());
        }
        return null;
    }

    private ReactorSubscriber passThisToSubscription(ReactorSubscriber reactorSubscriber, Object[] args) {
        ReactorSubscriberAccessor reactorSubscriberAccessor = ArrayArgumentUtils.getArgument(args, 0, ReactorSubscriberAccessor.class);
        if (reactorSubscriberAccessor != null) {
            final ReactorSubscriber subscriptionReactorSubscriber = new ReactorSubscriber(reactorSubscriber.getAsyncContext());
            ReactorSubscriberAccessorUtils.set(subscriptionReactorSubscriber, args, 0);
            if (isDebug) {
                logger.debug("Pass this to subscription(args[0])");
            }
            return subscriptionReactorSubscriber;
        }

        return null;
    }

    private ReactorSubscriber passThisToActual(ReactorSubscriber reactorSubscriber, Object target) {
        if (target instanceof ReactorActualAccessor) {
            final ReactorSubscriberAccessor reactorSubscriberAccessor = ((ReactorActualAccessor) target)._$PINPOINT$_getReactorActual();
            if (reactorSubscriberAccessor != null) {
                ReactorSubscriber actualReactorSubscriber = new ReactorSubscriber(reactorSubscriber.getAsyncContext());
                reactorSubscriberAccessor._$PINPOINT$_setReactorSubscriber(actualReactorSubscriber);
                if (isDebug) {
                    logger.debug("Pass this to actual(parent)");
                }
                return actualReactorSubscriber;
            }
        }

        return null;
    }

    private void onSubscribe(ReactorSubscriber reactorSubscriber, Object target) {
        reactorSubscriber.setSubscribe(Boolean.TRUE);
        AsyncContextAccessorUtils.setAsyncContext(reactorSubscriber.getAsyncContext(), target);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
