/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.loader.plugins.trace;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TraceMetadataProviderLoaderTest {

    private static final String TYPE_A_DEFINITION = "serviceTypes:\n"
            + "    - code: 1\n"
            + "      name: 'TYPE_A'\n";

    private static final String TYPE_B_DEFINITION = "serviceTypes:\n"
            + "    - code: 2\n"
            + "      name: 'TYPE_B'\n";

    // an IDE lists both a module's classes directory and its packaged jar on the classpath,
    // so the same definition file can be visible through two different URLs
    @Test
    public void identicalDefinitionsFromDifferentUrlsAreLoadedOnce(@TempDir Path tempDir) throws IOException {
        URL first = writeDefinition(tempDir, "first", TYPE_A_DEFINITION);
        URL second = writeDefinition(tempDir, "second", TYPE_A_DEFINITION);

        List<TraceMetadataProvider> providers = load(first, second);

        assertThat(providers).hasSize(1);
    }

    @Test
    public void distinctDefinitionsAreAllLoaded(@TempDir Path tempDir) throws IOException {
        URL first = writeDefinition(tempDir, "first", TYPE_A_DEFINITION);
        URL second = writeDefinition(tempDir, "second", TYPE_B_DEFINITION);

        List<TraceMetadataProvider> providers = load(first, second);

        assertThat(providers).hasSize(2);
    }

    private List<TraceMetadataProvider> load(URL... typeProviderUrls) {
        TraceMetadataProviderLoader loader = new TraceMetadataProviderLoader(Arrays.asList(typeProviderUrls));
        return loader.load(new URLClassLoader(new URL[0], null));
    }

    private URL writeDefinition(Path tempDir, String subDir, String definition) throws IOException {
        Path definitionFile = Files.createDirectory(tempDir.resolve(subDir)).resolve("type-provider.yml");
        Files.write(definitionFile, definition.getBytes(StandardCharsets.UTF_8));
        return definitionFile.toUri().toURL();
    }
}
