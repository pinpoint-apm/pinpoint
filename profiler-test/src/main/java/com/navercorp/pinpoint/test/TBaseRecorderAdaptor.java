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

package com.navercorp.pinpoint.test;

/**
 * @author emeroad
 */
public class TBaseRecorderAdaptor<T> implements ListenableDataSender.Listener<T> {

    private final TBaseRecorder<T> recorder;

    public TBaseRecorderAdaptor() {
        this.recorder = new TBaseRecorder<T>();
    }

    public TBaseRecorderAdaptor(TBaseRecorder<T> recorder) {
        this.recorder = recorder;
    }

    @Override
    public boolean handleSend(T data) {
        return recorder.add(data);
    }

    public TBaseRecorder<T> getRecorder() {
        return recorder;
    }
}
