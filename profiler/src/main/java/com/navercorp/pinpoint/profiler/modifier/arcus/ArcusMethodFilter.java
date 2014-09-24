package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import javassist.CtMethod;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

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
                "asyncBopDelete",
                "asyncBopGet",
                "asyncBopGetBulk",
                "asyncBopGetItemCount",
                "asyncBopInsert",
                "asyncBopInsertBulk",
                "asyncBopPipedInsertBulk",
                "asyncBopSortMergeGet",
                "asyncBopUpdate",
                "asyncGetAttr",
                "asyncLopCreate",
                "asyncLopDelete",
                "asyncLopGet",
                "asyncLopInsert",
                "asyncLopInsertBulk",
                "asyncLopPipedInsertBulk",
                "asyncSetAttr",
                "asyncSetBulk",
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
    public boolean filter(CtMethod ctMethod) {
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
