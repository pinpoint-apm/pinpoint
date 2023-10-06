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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class DeferredDisposableTest {

    @Test
    public void normallyDispose() {
        AtomicInteger value = new AtomicInteger(0);
        DeferredDisposable disposable = new DeferredDisposable();
        disposable.setDisposable(() -> value.getAndAdd(100));
        disposable.dispose();
        assertThat(value.get()).isEqualTo(100);
    }

    @Test
    public void settingDisposeAfterDispose() {
        DeferredDisposable disposable = new DeferredDisposable();
        disposable.dispose();

        AtomicInteger value = new AtomicInteger(0);
        disposable.setDisposable(() -> value.getAndAdd(100));
        assertThat(value.get()).isEqualTo(100);
    }

    @Test
    public void disposeMultipleTimes() {
        AtomicInteger value = new AtomicInteger(0);
        DeferredDisposable disposable = new DeferredDisposable();
        disposable.setDisposable(() -> value.getAndAdd(100));
        disposable.dispose();
        disposable.dispose();
        disposable.dispose();
        assertThat(value.get()).isEqualTo(100);
    }

}
