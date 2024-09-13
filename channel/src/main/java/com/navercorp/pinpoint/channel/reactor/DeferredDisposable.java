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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author youngjin.kim2
 *
 * Ensure delegated disposable is run at least once.
 * <br>
 * Even if the delegated disposable is set after the DeferredDisposable is disposed,
 * the delegated disposable is executed.
 */
public class DeferredDisposable implements Disposable {

    private static final Disposable UNINITIALIZED = () -> {
    };
    private static final Disposable COMPLETE = () -> {
    };

    private static final AtomicReferenceFieldUpdater<DeferredDisposable, Disposable> UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(DeferredDisposable.class, Disposable.class, "delegate");

    private volatile Disposable delegate = UNINITIALIZED;



    @Override
    public void dispose() {
        Disposable disposable = UPDATER.getAndSet(this, COMPLETE);
        disposable.dispose();
    }

    @Override
    public boolean isDisposed() {
        return UPDATER.get(this) == COMPLETE;
    }

    public void setDisposable(Disposable target) {
        if (!UPDATER.compareAndSet(this, UNINITIALIZED, target)) {
            target.dispose();
        }
    }

}
