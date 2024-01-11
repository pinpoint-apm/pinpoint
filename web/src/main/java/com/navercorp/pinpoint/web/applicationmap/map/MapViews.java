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
import com.navercorp.pinpoint.web.util.json.JsonViewUtils;

public interface MapViews {

    interface Simplified extends NodeViews.Simplified, LinkViews.Simplified {
        static boolean inView(Class<?> activeView) {
            return JsonViewUtils.inView(MapViews.Simplified.class, activeView);
        }
    }

    interface Basic extends NodeViews.Basic, LinkViews.Basic {
        static boolean inView(Class<?> activeView) {
            return JsonViewUtils.inView(MapViews.Basic.class, activeView);
        }
    }

    interface Detailed extends NodeViews.Detailed, LinkViews.Detailed {
        static boolean inView(Class<?> activeView) {
            return JsonViewUtils.inView(MapViews.Detailed.class, activeView);
        }
    }

}
