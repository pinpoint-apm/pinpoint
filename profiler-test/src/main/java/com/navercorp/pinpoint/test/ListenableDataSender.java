/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.profiler.sender.DataSender;
import org.apache.thrift.TBase;

/**
 * @author emeroad
 */
public class ListenableDataSender<T extends TBase<?, ?>> implements DataSender {

    private static final Listener DEFAULT_LISTENER = new Listener() {
        @Override
        public boolean handleSend(TBase<?, ?> data) {
            return true;
        }

        @Override
        public String toString() {
            return "EMPTY_LISTENER";
        }
    };

    private final String name;
    private volatile Listener listener = DEFAULT_LISTENER;

    public ListenableDataSender(String name) {
        this.name = name;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return listener.handleSend(data);
    }

    public Listener getListener() {
        return listener;
    }

    @Override
    public void stop() {
    }

    public interface Listener {
        boolean handleSend(TBase<?, ?> data);
    }

    @Override
    public String toString() {
        return "ListenableDataSender{" +
                "name='" + name + '\'' +
                ", listener=" + listener +
                '}';
    }
}
