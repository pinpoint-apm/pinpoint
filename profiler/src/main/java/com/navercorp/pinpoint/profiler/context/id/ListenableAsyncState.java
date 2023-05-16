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
package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.storage.DisabledUriStatStorage;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
@InterfaceAudience.LimitedPrivate("vert.x")
public class ListenableAsyncState implements AsyncState {

    private final LocalTraceRoot traceRoot;
    private final AsyncStateListener asyncStateListener;
    private final ActiveTraceHandle activeTraceHandle;
    private final UriStatStorage uriStatStorage;

    private boolean setup = false;
    private boolean await = false;
    private boolean finish = false;

    public ListenableAsyncState(LocalTraceRoot traceRoot,
                                AsyncStateListener asyncStateListener,
                                ActiveTraceHandle activeTraceHandle,
                                UriStatStorage uriStatStorage) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.asyncStateListener = Objects.requireNonNull(asyncStateListener, "asyncStateListener");
        this.activeTraceHandle = Objects.requireNonNull(activeTraceHandle, "activeTraceHandle");
        this.uriStatStorage = Objects.requireNonNull(uriStatStorage, "uriStatStorage");

    }

    @Override
    public void finish() {
        boolean finished = false;
        synchronized (this) {
            if (this.await && !this.finish) {
                finished = true;
            }
            this.finish = true;
        }
        if (finished) {
            this.asyncStateListener.finish();
            final long purgeTime = System.currentTimeMillis();
            this.activeTraceHandle.purge(purgeTime);
            storeUriTemplate(purgeTime);
        }
    }

    private void storeUriTemplate(long purgeTime) {
        if (uriStatStorage == DisabledUriStatStorage.INSTANCE) {
            return;
        }

        Shared shared = this.traceRoot.getShared();
        long traceStartTime = this.traceRoot.getTraceStartTime();
        boolean status = getStatus(shared.getErrorCode());
        this.uriStatStorage.store(shared.getUriTemplate(), status, traceStartTime, purgeTime);
    }

    private boolean getStatus(int errorCode) {
        if (errorCode == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void setup() {
        synchronized (this) {
            this.setup = true;
        }
    }

    @Override
    public boolean await() {
        final boolean await = await0();
        if (await == false) {
            final long purgeTime = System.currentTimeMillis();
            activeTraceHandle.purge(purgeTime);
            storeUriTemplate(purgeTime);
        }
        return await;
    }

    private boolean await0() {
        synchronized (this) {
            if (!this.setup || this.finish) {
                return false;
            }
            this.await = true;
            return true;
        }
    }

    @InterfaceAudience.LimitedPrivate("LocalTraceContext")
    public interface AsyncStateListener {
        AsyncStateListener EMPTY = new DisableAsyncStateListener();

        void finish();
    }

    static class DisableAsyncStateListener implements AsyncStateListener {
        @Override
        public void finish() {
        }
    }

    @Override
    public String toString() {
        return "ListenableAsyncState{" +
                "asyncStateListener=" + asyncStateListener +
                ", setup=" + setup +
                ", await=" + await +
                ", finish=" + finish +
                '}';
    }
}