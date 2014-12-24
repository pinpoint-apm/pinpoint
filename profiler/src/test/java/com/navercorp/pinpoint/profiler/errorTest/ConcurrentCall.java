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

package com.navercorp.pinpoint.profiler.errorTest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author emeroad
 */
@Ignore
public class ConcurrentCall {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String host = "http://localhost:8080";
    private String[] urls = new String[]{
            "/mysql", "/mysqlStatement",
            "/combination",
            "/donothing",
            "/oracle/selectOne", "/oracle/createStatement",
            "/mysql/selectOne", "/mysql/createStatement",
            "/arcus",
            "/nested"
    };
    private String pinpoint = ".pinpoint";

    private AtomicInteger id = new AtomicInteger(new Random().nextInt());
    private ExecutorService executorService = Executors.newFixedThreadPool(200);

    @Test
    public void test() throws IOException, InterruptedException {
        ((ThreadPoolExecutor) executorService).prestartAllCoreThreads();

        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);

        final HttpClient client = new DefaultHttpClient(cm);
        int call = 400;
        final CountDownLatch latch = new CountDownLatch(call);
        for (int i = 0; i < call; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String url = getUrl();
                        logger.info("execute {}", url);
                        final HttpGet httpGet = new HttpGet(url);
                        final HttpResponse execute = client.execute(httpGet);
                        execute.getEntity().getContent().close();

                    } catch (ClientProtocolException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        executorService.shutdown();
        cm.shutdown();


    }

    private String getUrl() {
        return host + getUrls() + pinpoint;
    }

    private String getUrls() {
        final int index = Math.abs(id.getAndIncrement() % urls.length);
        return urls[index];
    }
}
