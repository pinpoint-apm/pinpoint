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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfilePropertyLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void isAllowPinpointProperty() {
        PropertySnapshot properties = new PropertySnapshot(new Properties());

        properties.setProperty("pinpoint.test", "a");
        File root = folder.getRoot();
        ProfilePropertyLoader loader = new ProfilePropertyLoader(properties, root.getPath(), "test", new String[]{"test"});
        Assert.assertTrue(loader.isAllowPinpointProperty("pinpoint.test"));

        Assert.assertFalse(loader.isAllowPinpointProperty("unknown.test"));

    }
}