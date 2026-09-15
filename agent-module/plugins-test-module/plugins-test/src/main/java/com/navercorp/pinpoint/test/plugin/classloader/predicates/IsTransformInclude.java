package com.navercorp.pinpoint.test.plugin.classloader.predicates;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The classes a test asks to be transformed by the agent through {@code @TransformInclude}:
 * a trailing dot names a package prefix, anything else an exact class name.
 */
public class IsTransformInclude implements Predicate<String> {
    // array, not List: test() runs on every class load and the for-each over an array allocates no iterator
    private final String[] transformIncludes;

    public IsTransformInclude(List<String> transformIncludeList) {
        Objects.requireNonNull(transformIncludeList, "transformIncludeList");
        this.transformIncludes = transformIncludeList.toArray(new String[0]);
    }

    @Override
    public boolean test(String name) {
        for (String transformInclude : transformIncludes) {
            if (transformInclude.endsWith(".")) {
                if (name.startsWith(transformInclude)) {
                    return true;
                }
            } else {
                if (name.equals(transformInclude)) {
                    return true;
                }
            }
        }
        return false;
    }
}
