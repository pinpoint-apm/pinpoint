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

package com.navercorp.test.pinpoint.plugin.rxjava.service;

import com.navercorp.test.pinpoint.plugin.rxjava.repository.EchoRepository;
import rx.Single;
import rx.SingleSubscriber;

/**
 * @author HyunGil Jeong
 */
public class EchoService {

    private final EchoRepository echoRepository = new EchoRepository();

    public Single<String> echo(final String message) {
        return Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(SingleSubscriber<? super String> singleSubscriber) {
                if (!singleSubscriber.isUnsubscribed()) {
                    String echo = echoRepository.echo(message);
                    singleSubscriber.onSuccess(echo);
                }
            }
        });
    }

    public Single<String> echo(final String message, final Exception expected) {
        return Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(SingleSubscriber<? super String> singleSubscriber) {
                try {
                    if (!singleSubscriber.isUnsubscribed()) {
                        String echo = echoRepository.echo(message, expected);
                        singleSubscriber.onSuccess(echo);
                    }
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        });
    }

}
