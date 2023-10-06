/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.channel.reactor;

import reactor.core.Disposable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
public class DeferredDisposable implements Disposable {

    private static final Disposable UNINITIALIZED = () -> {
    };
    private static final Disposable COMPLETE = () -> {
    };

    private final AtomicReference<Disposable> delegateRef = new AtomicReference<>(UNINITIALIZED);

    @Override
    public void dispose() {
        this.delegateRef.getAndSet(COMPLETE).dispose();
    }

    @Override
    public boolean isDisposed() {
        return this.delegateRef.get() == COMPLETE;
    }

    public void setDisposable(Disposable target) {
        if (!this.delegateRef.compareAndSet(UNINITIALIZED, target)) {
            target.dispose();
        }
    }

}
