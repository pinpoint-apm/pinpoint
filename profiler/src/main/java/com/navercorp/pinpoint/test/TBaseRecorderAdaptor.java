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

import org.apache.thrift.TBase;

/**
 * @author emeroad
 */
public class TBaseRecorderAdaptor<T extends TBase<?, ?>> implements ListenableDataSender.Listener {

    private final TBaseRecorder<TBase<?, ?>> recorder;

    public TBaseRecorderAdaptor() {
        this.recorder = new TBaseRecorder<TBase<?, ?>>();
    }

    public TBaseRecorderAdaptor(TBaseRecorder<TBase<?, ?>> recorder) {
        this.recorder = recorder;
    }

    @Override
    public boolean handleSend(TBase<?, ?> data) {
        return recorder.add(data);
    }

    public TBaseRecorder<TBase<?, ?>> getRecorder() {
        return recorder;
    }
}
