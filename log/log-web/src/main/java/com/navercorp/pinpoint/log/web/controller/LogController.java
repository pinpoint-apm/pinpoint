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
import com.navercorp.pinpoint.log.vo.Log;
import com.navercorp.pinpoint.log.web.service.LiveTailService;
import com.navercorp.pinpoint.log.web.vo.LogHost;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
@RestController
@RequestMapping("/log")
public class LogController {

    private final LiveTailService service;

    public LogController(LiveTailService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @GetMapping("hostGroups/{hostGroup}/hosts/{host}/files/{file}/recent")
    public List<String> test(
            @PathVariable("hostGroup") String hostGroupName,
            @PathVariable("host") String hostName,
            @PathVariable("file") String fileName,
            @RequestParam("durationMillis") long durationMillis
    ) {
        final FileKey fileKey = FileKey.of(hostGroupName, hostName, fileName);
        final Duration duration = Duration.ofMillis(durationMillis);
        return this.service.tail(fileKey)
                .take(duration)
                .flatMap(pile -> Flux.fromIterable(pile.getLogs()))
                .map(Log::getLog)
                .collectList()
                .block();
    }

    @GetMapping("hostGroups")
    public Set<String> getHostGroups() {
        return this.service.getHostGroupNames();
    }

    @GetMapping("hostGroups/{hostGroup}/hosts")
    public List<LogHost> getHosts(@PathVariable("hostGroup") String hostGroupName) {
        return LogHost.from(this.service.getFileKeys(hostGroupName));
    }

}
