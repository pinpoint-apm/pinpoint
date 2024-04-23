/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util.time;

import java.time.Duration;
import java.time.Instant;

/**
 * @author emeroad
 */
public class ReverseRangeValidator implements RangeValidator {

    private final RangeValidator validator;

    public ReverseRangeValidator(Duration limitDay) {
        this.validator = new ForwardRangeValidator(limitDay);
    }

    @Override
    public void validate(Instant from, Instant to) {
        validator.validate(to, from);
    }

    @Override
    public void validate(Range range) {
        // Range already inverted
        validator.validate(range.getToInstant(), range.getFromInstant());
    }
}
