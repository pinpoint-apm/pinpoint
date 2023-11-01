/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.validation;

import com.navercorp.pinpoint.web.util.ValueValidator;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author youngjin.kim2
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Length(min = ValueValidator.USER_ID_MIN_LENGTH, max = ValueValidator.USER_ID_MAX_LENGTH)
@Pattern(regexp = ValueValidator.USER_ID_PATTERN_EXPRESSION)
public @interface UserId {
}
