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
package com.navercorp.pinpoint.realtime.collector.sink;

import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class IterativeErrorSinkRepository implements ErrorSinkRepository {

    private final Collection<? extends SinkRepository<? extends MonoSink<?>>> monoSinkRepositories;
    private final Collection<? extends SinkRepository<? extends FluxSink<?>>> fluxSinkRepositories;

    public IterativeErrorSinkRepository(
            Collection<? extends SinkRepository<? extends MonoSink<?>>> monoSinkRepositories,
            Collection<? extends SinkRepository<? extends FluxSink<?>>> fluxSinkRepositories
    ) {
        this.monoSinkRepositories = Objects.requireNonNull(monoSinkRepositories, "monoSinkRepositories");
        this.fluxSinkRepositories = Objects.requireNonNull(fluxSinkRepositories, "fluxSinkRepositories");
    }

    @Override
    public void error(long id, Throwable th) {
        MonoSink<?> monoSink = this.findMonoSink(id);
        if (monoSink != null) {
            monoSink.error(th);
            return;
        }

        FluxSink<?> fluxSink = this.findFluxSink(id);
        if (fluxSink != null) {
            fluxSink.error(th);
            return;
        }

        throw new RuntimeException("Failed to emit error: sink not found", th);
    }

    @Nullable
    private MonoSink<?> findMonoSink(long id) {
        for (SinkRepository<? extends MonoSink<?>> repo: this.monoSinkRepositories) {
            MonoSink<?> sink = repo.get(id);
            if (sink != null) {
                return sink;
            }
        }
        return null;
    }

    @Nullable
    private FluxSink<?> findFluxSink(long id) {
        for (SinkRepository<? extends FluxSink<?>> repo: this.fluxSinkRepositories) {
            FluxSink<?> sink = repo.get(id);
            if (sink != null) {
                return sink;
            }
        }
        return null;
    }

}
