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

package com.navercorp.pinpoint.web.performance;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Ignore;

/**
 *
 */
@Ignore
public class TestSuite {
    //    @Test
    public void insertData() throws IOException, InterruptedException {


        for (int i = 0; i < 10000; i++) {
            int mod = i % 4;
            if (mod == 0) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get1 = new HttpGet("http://localhost:8080/combination.pinpoint");
                HttpResponse execute = client.execute(get1);
            } else if (mod == 1) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get2 = new HttpGet("http://localhost:8080/mysql.pinpoint");
                HttpResponse execute = client.execute(get2);
            } else if (mod == 2) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get3 = new HttpGet("http://localhost:8080/donothing.pinpoint");
                HttpResponse execute = client.execute(get3);
            } else if (mod == 3) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get4 = new HttpGet("http://localhost:8080/combination.pinpoint");
                HttpResponse execute = client.execute(get4);
            }
        }

    }
}
