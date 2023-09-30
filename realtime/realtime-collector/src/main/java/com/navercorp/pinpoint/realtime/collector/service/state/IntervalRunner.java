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
package com.navercorp.pinpoint.realtime.collector.service.state;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
public class IntervalRunner implements InitializingBean, DisposableBean {

    private final Runnable runnable;
    private final Duration period;
    private final Scheduler scheduler;

    private final AtomicReference<Disposable> disposableRef = new AtomicReference<>();

    public IntervalRunner(Runnable runnable, Duration period, Scheduler scheduler) {
        this.runnable = Objects.requireNonNull(runnable, "runnable");
        this.period = Objects.requireNonNull(period, "period");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @Override
    public void destroy() {
        Disposable disposable = this.disposableRef.get();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Disposable disposable = Flux.interval(this.period, this.scheduler).subscribe(t -> this.runnable.run());
        if (!disposableRef.compareAndSet(null, disposable)) {
            disposable.dispose();
        }
    }

}
