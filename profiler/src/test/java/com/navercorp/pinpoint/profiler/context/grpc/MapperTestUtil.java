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
package com.navercorp.pinpoint.profiler.context.grpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author intr3p1d
 */
public class MapperTestUtil {

    public static Map<Integer, String> randomIntegerStringMap(){
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, randomString());
        }
        return map;
    }

    public static List<String> randomStringList() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add(randomString());
        }
        return strings;
    }

    public static String randomString() {
        UUID randomUUID = UUID.randomUUID();
        return randomUUID.toString();
    }

}
