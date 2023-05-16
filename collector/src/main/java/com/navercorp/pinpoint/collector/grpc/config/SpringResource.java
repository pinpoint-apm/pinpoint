/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.grpc.util.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpringResource implements Resource {

    private final org.springframework.core.io.Resource resource;

    public SpringResource(org.springframework.core.io.Resource resource) {
        this.resource = Objects.requireNonNull(resource, "resource");
    }

    @Override
    public boolean exists() {
        return resource.exists();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public File getFile() throws IOException {
        return resource.getFile();
    }

    @Override
    public URL getURL() throws IOException {
        return resource.getURL();
    }

    @Override
    public String toString() {
        return "SpringResource{" +
                "resource=" + resource +
                '}';
    }
}
