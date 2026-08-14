/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.reactorsupport;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Downstream test subscriber that negotiates fusion when offered and records everything it sees.
 */
class TestFusionSubscriber<T> implements CoreSubscriber<T> {
    final List<T> received = new ArrayList<T>();
    Subscription subscription;
    int negotiatedFusionMode = Integer.MIN_VALUE;
    boolean completed;
    Throwable error;
    private final Context context;

    TestFusionSubscriber() {
        this(Context.empty());
    }

    TestFusionSubscriber(Context context) {
        this.context = context;
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        if (s instanceof Fuseable.QueueSubscription) {
            this.negotiatedFusionMode = ((Fuseable.QueueSubscription<?>) s).requestFusion(Fuseable.ANY);
        }
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        received.add(t);
    }

    @Override
    public void onError(Throwable t) {
        this.error = t;
    }

    @Override
    public void onComplete() {
        this.completed = true;
    }

    @Override
    public Context currentContext() {
        return context;
    }
}
