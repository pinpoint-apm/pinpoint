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

package com.navercorp.pinpoint.plugin.rxjava;

import com.navercorp.pinpoint.plugin.rxjava.runners.ConnectableObservableTestRunner;
import com.navercorp.pinpoint.plugin.rxjava.runners.GroupedObservableTestRunner;
import com.navercorp.pinpoint.plugin.rxjava.runners.ObservableTestRunner;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
// rxjava plugin enabled + custom trace method config
@PinpointConfig("rxjava/pinpoint-rxjava.config")
@Dependency({"io.reactivex:rxjava:[1.0.0,1.1.0]"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-rxjava-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
public class RxJava_1_0_0_to_1_1_0_IT {

    private final ObservableTestRunner observableTestRunner = new ObservableTestRunner();
    private final ConnectableObservableTestRunner connectableObservableTestRunner = new ConnectableObservableTestRunner();
    private final GroupedObservableTestRunner groupedObservableTestRunner = new GroupedObservableTestRunner();

    @Test
    public void observable() throws Exception {
        observableTestRunner.observable();
    }

    @Test
    public void observableError() throws Exception {
        observableTestRunner.observableError();
    }

    @Test
    public void blockingObservable() throws Exception {
        observableTestRunner.blockingObservable();
    }

    @Test
    public void connectableObservable() throws Exception {
        connectableObservableTestRunner.connectableObservable();
    }

    @Test
    public void connectableObservableError() throws Exception {
        connectableObservableTestRunner.connectableObservableError();
    }

    @Test
    public void groupedObservable() throws Exception {
        groupedObservableTestRunner.groupedObservable();
    }
}
