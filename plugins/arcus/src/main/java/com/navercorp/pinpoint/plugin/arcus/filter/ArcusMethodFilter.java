package com.navercorp.pinpoint.plugin.arcus.filter;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

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
    public boolean filter(MethodInfo ctMethod) {
        final int modifiers = ctMethod.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return true;
        }
        if (WHITE_LIST_API.get(ctMethod.getName()) == FIND) {
            return false;
        }
        return true;
    }
}
