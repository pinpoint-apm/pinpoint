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
package com.navercorp.pinpoint.web.realtime.activethread.count.websocket;

import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.Disposable;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
final class SessionContext {

    private static final String KEY_ATTR = "atcAttrs";

    private final @Nullable TimerTaskDecorator taskDecorator;

    private final Lock lock = new ReentrantLock();
    private final long sessionCreatedAt = System.currentTimeMillis();
    private Disposable subscription = null;

    private SessionContext(@Nullable TimerTaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }

    static void newContext(WebSocketSession session, TimerTaskDecoratorFactory taskDecoratorFactory) {
        final SessionContext ctx = new SessionContext(getTimerTaskDecorator(taskDecoratorFactory));
        session.getAttributes().put(KEY_ATTR, ctx);
    }

    private static TimerTaskDecorator getTimerTaskDecorator(TimerTaskDecoratorFactory taskDecoratorFactory) {
        if (taskDecoratorFactory == null) {
            return null;
        }
        return taskDecoratorFactory.createTimerTaskDecorator();
    }

    static void runWithLockedContext(WebSocketSession session, Consumer<SessionContext> consumer) {
        final SessionContext ctx = get(session);
        if (ctx != null) {
            final Lock lock = ctx.lock;
            lock.lock();
            consumer.accept(ctx);
            lock.unlock();
        }
    }

    static void dispose(WebSocketSession session) {
        runWithLockedContext(session, ctx -> {
            final Disposable subscription = ctx.getSubscription();
            if (subscription != null) {
                subscription.dispose();
            }
        });
    }

    long getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    @Nullable
    TimerTaskDecorator getTaskDecorator() {
        return taskDecorator;
    }

    private Disposable getSubscription() {
        return subscription;
    }

    void setSubscription(Disposable newSubscription) {
        final Disposable prevSubscription = this.subscription;
        this.subscription = newSubscription;
        if (prevSubscription != null) {
            prevSubscription.dispose();
        }
    }

    static SessionContext get(WebSocketSession session) {
        return (SessionContext) session.getAttributes().get(KEY_ATTR);
    }

}
