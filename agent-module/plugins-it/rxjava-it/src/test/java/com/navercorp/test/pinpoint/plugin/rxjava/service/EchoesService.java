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
import rx.Observable;
import rx.Subscriber;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class EchoesService {

    private final EchoRepository echoRepository = new EchoRepository();

    public Observable<String> echo(final List<String> messages) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    for (String message : messages) {
                        String echo = echoRepository.echo(message);
                        subscriber.onNext(echo);
                    }
                    subscriber.onCompleted();
                }
            }
        });
    }

    public Observable<String> echo(final List<String> messages, final Exception expected) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        for (String message : messages) {
                            String echo = echoRepository.echo(message, expected);
                            subscriber.onNext(echo);
                        }
                        subscriber.onCompleted();
                    }
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
