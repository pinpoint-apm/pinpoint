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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.vo.Range;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author emeroad
 */
public class ApplicationMapTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void root() throws IOException {
        ApplicationMap app = new DefaultApplicationMap(new Range(0, 1), new NodeList(), new LinkList());
        String s = MAPPER.writeValueAsString(app);
        logger.debug(s);

    }
}
