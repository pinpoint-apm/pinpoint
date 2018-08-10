/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.test.pinpoint.plugin.rabbitmq.spring;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public class TestMessageHolder {

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<String>();

    public void addMessage(String message) {
        messages.add(message);
    }

    public String getMessage(long timeoutMs, TimeUnit unit) throws InterruptedException {
        return messages.poll(timeoutMs, unit);
    }
}
