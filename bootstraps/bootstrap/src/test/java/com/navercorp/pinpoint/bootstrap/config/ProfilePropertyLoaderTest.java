/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.util.PropertySnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfilePropertyLoaderTest {
    @TempDir
    public Path temp;

    @Test
    public void isAllowPinpointProperty() throws IOException {
        PropertySnapshot properties = new PropertySnapshot(new Properties());

        properties.setProperty("pinpoint.test", "a");
        File root = new File(temp.toString());
        ProfilePropertyLoader loader = new ProfilePropertyLoader(properties, properties, root.toPath(), Paths.get("test"), new String[]{"test"});
        Assertions.assertTrue(loader.isAllowPinpointProperty("pinpoint.test"));

        Assertions.assertFalse(loader.isAllowPinpointProperty("unknown.test"));

    }
}