/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.profiler.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hyunjoon Cho
 */
@RestController
public class ContainerProfilerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value = "/container-profiler/cpuLoad")
    @ResponseBody
    public String putCpuLoad(@RequestParam("percent") long percent) {
        long startTime = System.currentTimeMillis();
        long currentTime;
        if (percent < 0 || percent > 100){
            return "NOPE";
        }

         do{
            currentTime = System.currentTimeMillis();
            if(currentTime % 100 == 0) {
//                logger.info("Sleep at {}", currentTime);
                sleep(100 - percent);
            }
        }while (currentTime - startTime < 30000);

        return "OK";
    }

    @GetMapping(value = "/container-profiler/memoryLoad")
    public String putMemoryLoad() {
        byte[] bytes = new byte[100000000];

        for(int i=0; i<100000000; i++){
            bytes[i] = 1;
        }

        return "OK";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}