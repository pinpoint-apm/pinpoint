/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value={"/error"})
@Validated
public class NonWhiteLabelErrorController extends AbstractErrorController {
    private final ErrorProperties errorProperties;

    public NonWhiteLabelErrorController(
            @Autowired ErrorAttributes errorAttributes,
            @Autowired ServerProperties serverProperties
    ) {
        super(errorAttributes);
        this.errorProperties = serverProperties.getError();
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        final HttpStatus status = this.getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        } else {
            final Map<String, Object> body = this.getErrorAttributes(request, this.getErrorAttributeOptions(request));
            return new ResponseEntity<>(body, status);
        }
    }

    private ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request) {
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        if (this.errorProperties.isIncludeException()) {
            options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
        }

        if (this.isIncludeStackTrace(request)) {
            options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
        }

        if (this.isIncludeMessage(request)) {
            options = options.including(ErrorAttributeOptions.Include.MESSAGE);
        }

        if (this.isIncludeBindingErrors(request)) {
            options = options.including(ErrorAttributeOptions.Include.BINDING_ERRORS);
        }

        return options;
    }

    private boolean isIncludeStackTrace(HttpServletRequest request) {
        return switch (this.getErrorProperties().getIncludeStacktrace()) {
            case ALWAYS -> true;
            case ON_PARAM -> this.getTraceParameter(request);
            default -> false;
        };
    }

    private boolean isIncludeMessage(HttpServletRequest request) {
        return switch (this.getErrorProperties().getIncludeMessage()) {
            case ALWAYS -> true;
            case ON_PARAM -> this.getMessageParameter(request);
            default -> false;
        };
    }

    private boolean isIncludeBindingErrors(HttpServletRequest request) {
        return switch (this.getErrorProperties().getIncludeBindingErrors()) {
            case ALWAYS -> true;
            case ON_PARAM -> this.getErrorsParameter(request);
            default -> false;
        };
    }

    private ErrorProperties getErrorProperties() {
        return this.errorProperties;
    }
}
