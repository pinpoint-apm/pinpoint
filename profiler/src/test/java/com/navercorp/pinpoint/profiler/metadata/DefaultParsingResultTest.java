/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.profiler.metadata.DefaultParsingResult;
import org.junit.Assert;
import org.junit.Test;

public class DefaultParsingResultTest {

    @Test
    public void testId() throws Exception {
        DefaultParsingResult result = new DefaultParsingResult("");
        Assert.assertEquals(ParsingResult.ID_NOT_EXIST, result.getId());

        // update
        Assert.assertTrue(result.setId(1));

        // already updated
        Assert.assertFalse(result.setId(1));
    }
}

