/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.common.servlet.util;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServletArgumentValidatorTest {

    private ArgumentValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        PLogger logger = PLoggerFactory.getLogger(this.getClass());
        this.validator = new ServletArgumentValidator(logger, 0, ServletRequest.class, 1, ServletResponse.class);
    }


    @Test
    public void valid() {
        Object[] argument = new Object[2];
        argument[0] = mock(ServletRequest.class);
        argument[1] = mock(ServletResponse.class);
        Assertions.assertTrue(validator.validate(argument));
    }

    @Test
    public void valid_boundary_check() {

        Assertions.assertFalse(validator.validate(null));
        Assertions.assertFalse(validator.validate(new Object[0]));
        Assertions.assertFalse(validator.validate(new Object[1]));

        Object[] argument = new Object[10];
        argument[0] = mock(ServletRequest.class);
        argument[1] = mock(ServletResponse.class);
        Assertions.assertTrue(validator.validate(argument));
    }

    @Test
    public void valid_fail1() {
        Object[] argument = new Object[2];
        argument[0] = mock(ServletRequest.class);
        argument[1] = new Object();
        Assertions.assertFalse(validator.validate(argument));
    }


    @Test
    public void valid_fail2() {

        Object[] argument = new Object[2];
        argument[0] = new Object();
        argument[1] = mock(ServletResponse.class);
        Assertions.assertFalse(validator.validate(argument));
    }
}