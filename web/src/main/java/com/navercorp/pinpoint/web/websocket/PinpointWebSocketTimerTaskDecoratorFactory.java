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

import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.TimerTask;

/**
 * @author HyunGil Jeong
 */
public class PinpointWebSocketTimerTaskDecoratorFactory implements TimerTaskDecoratorFactory {

    @Override
    public TimerTaskDecorator createTimerTaskDecorator() {
        return new SecurityContextPreservingTimerTaskDecorator();
    }

    private static class SecurityContextPreservingTimerTaskDecorator implements TimerTaskDecorator {

        private final SecurityContext securityContext;

        private SecurityContextPreservingTimerTaskDecorator() {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            SecurityContext currentSecurityContext = SecurityContextHolder.getContext();
            Authentication authentication = currentSecurityContext.getAuthentication();
            if (authentication != null) {
                securityContext.setAuthentication(authentication);
            }
            this.securityContext = securityContext;
        }

        @Override
        public TimerTask decorate(TimerTask timerTask) {
            return new TimerTask() {
                @Override
                public void run() {
                    SecurityContext previousSecurityContext = SecurityContextHolder.getContext();
                    try {
                        SecurityContextHolder.setContext(securityContext);
                        timerTask.run();
                    } finally {
                        SecurityContextHolder.setContext(previousSecurityContext);
                    }
                }
            };
        }
    }
}
