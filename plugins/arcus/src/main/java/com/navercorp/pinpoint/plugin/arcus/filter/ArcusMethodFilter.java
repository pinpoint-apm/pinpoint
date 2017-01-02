/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.arcus.filter;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;

/**
 * @author emeroad
 */
public class ArcusMethodFilter implements MethodFilter {
    private final static Object FIND = new Object();
    private final static Map<String, Object> WHITE_LIST_API;

    static {
        WHITE_LIST_API = createRule();
    }

    private static Map<String, Object> createRule() {
        String[] apiList = {
                "asyncBopCreate",
                "asyncBopDecr",
                "asyncBopDelete",
                "asyncBopFindPosition",
                "asyncBopFindPositionWithGet",
                "asyncBopGet",
                "asyncBopGetBulk",
                "asyncBopGetByPosition",
                "asyncBopGetItemCount",
                "asyncBopIncr",
                "asyncBopInsert",
                "asyncBopInsertAndGetTrimmed",
                "asyncBopInsertBulk",
                "asyncBopPipedInsertBulk",
                "asyncBopPipedUpdateBulk",
                "asyncBopSortMergeGet",
                "asyncBopUpdate",
                "asyncBopUpsert",
                "asyncBopUpsertAndGetTrimmed",
                "asyncGetAttr",
                "asyncLopCreate",
                "asyncLopDelete",
                "asyncLopGet",
                "asyncLopInsert",
                "asyncLopInsertBulk",
                "asyncLopPipedInsertBulk",
                "asyncSetAttr",
                "asyncSetBulk",
                "asyncSetPipedExist",
                "asyncSopCreate",
                "asyncSopDelete",
                "asyncSopExist",
                "asyncSopGet",
                "asyncSopInsert",
                "asyncSopInsertBulk",
                "asyncSopPipedExistBulk",
                "asyncSopPipedInsertBulk"
        };
        Map<String, Object> rule = new HashMap<String, Object>();
        for (String api : apiList) {
            rule.put(api, FIND);
        }
        return rule;
    }

    public ArcusMethodFilter() {
    }

    @Override
    public boolean accept(InstrumentMethod ctMethod) {
        final int modifiers = ctMethod.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return REJECT;
        }
        if (WHITE_LIST_API.get(ctMethod.getName()) == FIND) {
            return ACCEPT;
        }
        return REJECT;
    }
}
