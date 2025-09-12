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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.common.server.task.TaskDecoratorFactory;
import com.navercorp.pinpoint.user.util.SecurityContextUtils;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author HyunGil Jeong
 */
public class WebSocketTaskDecoratorFactory implements TaskDecoratorFactory {

    @Override
    public TaskDecorator createDecorator() {
        return new SecurityContextPreservingTaskDecorator();
    }

    private static class SecurityContextPreservingTaskDecorator implements TaskDecorator {

        private final SecurityContext securityContext;

        private SecurityContextPreservingTaskDecorator() {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            final Authentication authentication = SecurityContextUtils.getAuthentication();
            if (authentication != null) {
                securityContext.setAuthentication(authentication);
            }
            this.securityContext = securityContext;
        }

        @Override
        public Runnable decorate(Runnable task) {
            return new Runnable() {
                @Override
                public void run() {
                    SecurityContext previousSecurityContext = SecurityContextHolder.getContext();
                    try {
                        SecurityContextHolder.setContext(securityContext);
                        task.run();
                    } finally {
                        SecurityContextHolder.setContext(previousSecurityContext);
                    }
                }
            };
        }
    }
}
