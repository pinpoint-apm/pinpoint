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

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.web.applicationmap.link.LinkViews;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeViews;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapViewsTest {
    @Test
    void extend_test() {
        Assertions.assertTrue(MapViews.Simplified.inView(MapViews.Simplified.class));
        Assertions.assertTrue(LinkViews.Simplified.inView(MapViews.Simplified.class));

        Assertions.assertFalse(MapViews.Simplified.inView(MapViews.Detailed.class));
    }



    @Test
    void extend_test_failure() {
        Assertions.assertFalse(MapViews.Simplified.inView(NodeViews.Simplified.class));
        Assertions.assertFalse(MapViews.Simplified.inView(LinkViews.Simplified.class));

    }
}