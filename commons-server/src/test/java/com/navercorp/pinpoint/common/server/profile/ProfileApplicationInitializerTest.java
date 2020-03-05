/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.profile;

import com.navercorp.pinpoint.common.util.SystemProperty;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfileApplicationInitializerTest {

    public static final String DEFAULT_PROFILE = "default-profile";
    public static final String SYSTEM_PROFILE = "system-profile";

    @Test
    public void onStartup_defaultValue() {
        SystemProperty systemProperty = mock(SystemProperty.class);

        ProfileApplicationInitializer initializer = new ProfileApplicationInitializer(this.getClass().getSimpleName(), systemProperty, DEFAULT_PROFILE);
        initializer.onStartup();

        verify(systemProperty).setProperty(ProfileApplicationInitializer.ACTIVE_PROFILES_PROPERTY_NAME, DEFAULT_PROFILE);
    }

    @Test
    public void onStartup_system_property() {
        SystemProperty systemProperty = mock(SystemProperty.class);
        when(systemProperty.getProperty(ProfileApplicationInitializer.ACTIVE_PROFILES_PROPERTY_NAME)).thenReturn(SYSTEM_PROFILE);

        ProfileApplicationInitializer initializer = new ProfileApplicationInitializer(this.getClass().getSimpleName(), systemProperty, DEFAULT_PROFILE);
        initializer.onStartup();

        verify(systemProperty).setProperty(ProfileApplicationInitializer.ACTIVE_PROFILES_PROPERTY_NAME, SYSTEM_PROFILE);
    }
}