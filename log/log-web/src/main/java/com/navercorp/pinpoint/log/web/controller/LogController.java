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
package com.navercorp.pinpoint.log.web.controller;

import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.web.service.LiveTailService;
import com.navercorp.pinpoint.log.web.vo.LiveTailBatch;
import com.navercorp.pinpoint.log.web.vo.LogHostGroupInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
@RestController
@RequestMapping("log")
public class LogController {

    private final Logger logger = LogManager.getLogger(LogController.class);

    private final LiveTailService service;

    public LogController(LiveTailService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @GetMapping("hostGroups/{hostGroup}/tail")
    public ResponseEntity<SseEmitter> tailHostGroup(
            @PathVariable("hostGroup") String hostGroupName,
            @RequestParam(required = false) List<String> hostNames,
            @RequestParam(required = false) List<String> fileNames
    ) {
        List<FileKey> fileKeys = this.service.getFileKeys(hostGroupName, hostNames, fileNames);
        return tailSse(fileKeys);
    }

    @GetMapping("hostGroups")
    public Set<String> getHostGroups() {
        return this.service.getHostGroupNames();
    }

    @GetMapping("hostGroups/{hostGroup}")
    public LogHostGroupInfo getHosts(@PathVariable("hostGroup") String hostGroupName) {
        return LogHostGroupInfo.compose(this.service.getFileKeys(hostGroupName));
    }

    private ResponseEntity<SseEmitter> tailSse(List<FileKey> fileKeys) {
        Flux<List<LiveTailBatch>> tail = this.service.tail(fileKeys);
        if (tail == null) {
            return ResponseEntity.notFound().build();
        }

        Duration duration = Duration.ofSeconds(30);
        SseEmitter emitter = new SseEmitter(duration.toMillis());

        try {
            emitter.send(SseEmitter.event().comment("key found"));
        } catch (IOException e) {
            logger.error("Failed to send first comment", e);
            return ResponseEntity.internalServerError().build();
        }

        tail
                .take(duration)
                .subscribe(batch -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .reconnectTime(duration.toMillis())
                                .name("tail-batch")
                                .data(batch, MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        logger.error("Failed to send tail batch", e);
                        throw new RuntimeException(e);
                    }
                });
        return ResponseEntity.ok(emitter);
    }

}
