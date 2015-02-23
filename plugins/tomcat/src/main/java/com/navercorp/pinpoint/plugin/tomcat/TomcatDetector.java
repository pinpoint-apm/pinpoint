/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.tomcat;

import java.io.File;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ServerTypeDetector;
import com.navercorp.pinpoint.common.util.SystemProperty;

/**
 * @author Jongho Moon
 *
 */
public class TomcatDetector implements ServerTypeDetector, TomcatConstants {
    private final PLogger logger = PLoggerFactory.getLogger(getClass()); 
    
    @Override
    public String getServerTypeName() {
        return TYPE_NAME;
    }

    public boolean detect() {
        String homeDir = SystemProperty.INSTANCE.getProperty("catalina.home");
        
        if (homeDir == null) {
            logger.debug("catalina.home is not defined. This is not a Tomcat instance");
            return false;
        }
        
        File catalinaJar = new File(homeDir, "/lib/catalina.jar");
        
        if (!catalinaJar.exists()) {
            logger.debug(catalinaJar + " is not exist. This is not a Tomcat instance");
            return false;
        }
        
        logger.debug("catalina.home (" + homeDir + ") is defined and " + catalinaJar + " is exist. This is a Tomcat instance");
        
        return catalinaJar.exists();
    }

    @Override
    public boolean canOverride(String serverType) {
        return false;
    }
}
