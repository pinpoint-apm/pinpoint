/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.manager;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.springframework.boot.ApplicationArguments;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ProgramOptions {

    public static final String NAMESPACE = "namespace";
    public static final String COMPRESSION = "compression";
    public static final String DRY_RUN = "dry";

    private final String namespace;
    private final String compression;

    private ProgramOptions(String namespace, String compression) {
        this.namespace = namespace;
        this.compression = compression;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getCompression() {
        return compression;
    }

    public static ProgramOptions parseArgs(ApplicationArguments args) {
        final String namespace = getNamespace(args);
        final String compression = getCompression(args);
        return new ProgramOptions(namespace, compression);
    }

    private static String getNamespace(ApplicationArguments args) {
        List<String> namespaces = args.getOptionValues(NAMESPACE);
        if (CollectionUtils.isEmpty(namespaces)) {
            return NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR;
        }
        String namespace = namespaces.get(0);
        if (StringUtils.isEmpty(namespace)) {
            return NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR;
        }
        return namespace;
    }

    private static String getCompression(ApplicationArguments args) {
        List<String> compressions = args.getOptionValues(COMPRESSION);
        if (CollectionUtils.isEmpty(compressions)) {
            return Compression.Algorithm.NONE.getName();
        }
        String compression = compressions.get(0);
        if (StringUtils.isEmpty(compression)) {
            return Compression.Algorithm.NONE.getName();
        }
        return compression;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProgramOptions{");
        sb.append("namespace='").append(namespace).append('\'');
        sb.append(", compression='").append(compression).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
