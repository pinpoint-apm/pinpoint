/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.web.problem;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;

import java.net.URI;
import java.util.Map;

/**
 * @author intr3p1d
 */
public class ProblemWrapper implements Problem {

    @JsonUnwrapped
    private final Problem originalProblem;
    private final String hostName;
    private final String path;
    private final String method;

    public ProblemWrapper(Problem originalProblem, String hostName, String path, String method) {
        this.originalProblem = originalProblem;
        this.hostName = hostName;
        this.path = path;
        this.method = method;
    }

    @Override
    public URI getType() {
        return originalProblem.getType();
    }

    @Override
    public @Nullable String getTitle() {
        return originalProblem.getTitle();
    }

    @Override
    public @Nullable StatusType getStatus() {
        return originalProblem.getStatus();
    }

    @Override
    public @Nullable String getDetail() {
        return originalProblem.getDetail();
    }

    @Override
    public @Nullable URI getInstance() {
        return originalProblem.getInstance();
    }

    @Override
    public Map<String, Object> getParameters() {
        return originalProblem.getParameters();
    }

    public String getHostName() {
        return hostName;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}
