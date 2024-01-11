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

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.util.json.JsonViewUtils;

public interface NodeViews {

    interface Simplified {
        static boolean inView(Class<?> activeView) {
            return JsonViewUtils.inView(Simplified.class, activeView);
        }
    }

    interface Basic {
        static boolean inView(Class<?> activeView) {
            return JsonViewUtils.inView(Basic.class, activeView);
        }
    }

    interface Detailed {
        static boolean inView(Class<?> activeView) {
            return JsonViewUtils.inView(Detailed.class, activeView);
        }
    }


    static Class<?> getActiveView(SerializerProvider provider) {
        final Class<?> activeView = provider.getActiveView();
        if (activeView != null) {
            return activeView;
        }
        return NodeViews.Detailed.class;
    }
}
