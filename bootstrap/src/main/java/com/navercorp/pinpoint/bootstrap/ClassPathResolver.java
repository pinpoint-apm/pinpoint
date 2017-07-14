/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap;

import java.net.URL;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ClassPathResolver {

    boolean verify();

    BootstrapJarFile getBootstrapJarFile();

    String getPinpointCommonsJar();

    String getBootStrapCoreJar();

    String getBootStrapCoreOptionalJar();

    String getAgentJarName();

    String getAgentJarFullPath();

    String getAgentLibPath();

    String getAgentLogFilePath();

    String getAgentPluginPath();

    List<URL> resolveLib();

    URL[] resolvePlugins();

    String getAgentDirPath();

    String getAgentConfigPath();
}
