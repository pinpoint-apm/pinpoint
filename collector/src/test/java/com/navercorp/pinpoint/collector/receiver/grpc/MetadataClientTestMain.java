/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MetadataClientTestMain {

    public static void main(String[] args) throws Exception {
        MetadataClientMock clientMock = new MetadataClientMock("localhost", 9997, true);
        clientMock.apiMetaData(100);

        TimeUnit.SECONDS.sleep(60);
        List<String> list = clientMock.getResponseList();
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1) - Integer.valueOf(o2);
            }
        });

        System.out.println("Response size=" + list.size());
        for (String response : list) {
            System.out.println(response);
        }

        TimeUnit.SECONDS.sleep(60 * 60);
    }

}
