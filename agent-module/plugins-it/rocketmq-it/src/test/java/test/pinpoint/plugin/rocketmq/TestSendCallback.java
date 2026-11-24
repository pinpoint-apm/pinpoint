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
package test.pinpoint.plugin.rocketmq;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestSendCallback implements SendCallback {

    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile SendResult sendResult;
    private volatile Throwable throwable;

    @Override
    public void onSuccess(SendResult sendResult) {
        this.sendResult = sendResult;
        this.latch.countDown();
    }

    @Override
    public void onException(Throwable e) {
        this.throwable = e;
        this.latch.countDown();
    }

    public SendResult awaitSendResult(long timeoutMillis) throws Exception {
        if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            throw new IllegalStateException("SendCallback not invoked within " + timeoutMillis + "ms");
        }
        if (throwable != null) {
            throw new IllegalStateException("async send failed", throwable);
        }
        return sendResult;
    }
}
