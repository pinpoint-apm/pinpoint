/*
 * Copyright 2024 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeViews;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class LinkViewsTest {
    @Test
    public void basic() {
        Assertions.assertTrue(LinkViews.Basic.inView(LinkViews.Basic.class));
    }

    @Test
    public void basic_map() {
        Assertions.assertTrue(LinkViews.Basic.inView(MapViews.Basic.class));

        Assertions.assertFalse(LinkViews.Basic.inView(MapViews.Detailed.class));
    }

    @Test
    public void basic_failure() {
        Assertions.assertFalse(LinkViews.Basic.inView(LinkViews.Detailed.class));
        Assertions.assertFalse(LinkViews.Basic.inView(NodeViews.Detailed.class));
    }
}