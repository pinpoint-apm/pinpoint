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
package com.navercorp.pinpointdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author youngjin.kim2
 */
public class ClosedService {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void fire() {
        try {
            List<Integer> data = new ArrayList<>(16);
            for (int i = 0; i < 16; i++) {
                data.add(i);
            }
            data.parallelStream()
                    .map(v -> parserTask(v))
                    .forEach(this.executor::submit);
        } catch (Exception ignored) {
            System.out.println("error");
        }
    }

    private Runnable parserTask(int v) {
        return () -> System.out.println(v + "^2=" + (v*v));
    }

}
