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

package com.navercorp.pinpoint.web.task;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class SecurityContextPropagatingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return new SecurityContextPropagatingRunnable(runnable);
    }

    private static class SecurityContextPropagatingRunnable implements Runnable {

        private final Runnable delegate;
        private final SecurityContext securityContext;

        private SecurityContextPropagatingRunnable(Runnable delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
            this.securityContext = SecurityContextHolder.getContext();
        }

        @Override
        public void run() {
            SecurityContext previousSecurityContext = SecurityContextHolder.getContext();
            SecurityContextHolder.setContext(securityContext);
            try {
                delegate.run();
            } finally {
                SecurityContextHolder.setContext(previousSecurityContext);
            }
        }
    }
}
