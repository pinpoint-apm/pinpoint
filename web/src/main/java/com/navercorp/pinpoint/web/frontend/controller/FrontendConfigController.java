/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.frontend.controller;

import com.navercorp.pinpoint.web.frontend.export.FrontendConfigExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@RestController
public class FrontendConfigController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<FrontendConfigExporter> exporters;

    public FrontendConfigController(List<FrontendConfigExporter> exporters) {
        this.exporters = Objects.requireNonNull(exporters, "exporters");

        exporters.forEach(exporter ->
                logger.info("FrontendConfigExporter {}", exporter.getClass().getName())
        );
    }

    @GetMapping(value = "/configuration")
    public Map<String, Object> getProperties() {

        Map<String, Object> result = new LinkedHashMap<>();
        for (FrontendConfigExporter configExporter : exporters) {
            configExporter.export(result);
        }
        return result;
    }
}