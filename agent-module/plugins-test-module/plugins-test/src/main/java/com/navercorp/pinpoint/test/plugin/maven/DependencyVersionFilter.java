/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.maven;


import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DependencyVersionFilter implements Predicate<String> {
    public static boolean FILTERED = true;
    public static boolean NOT_FILTERED = false;

    // one alternation instead of one Pattern per qualifier: test() then allocates a single Matcher per version
    private static final Pattern PRE_RELEASE_PATTERN = Pattern.compile(
            ".*(?:rc|m|alpha|beta|patch|test|milestone|pre)-?\\.?\\d*$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean test(String value) {
        if (PRE_RELEASE_PATTERN.matcher(value).matches()) {
            return FILTERED;
        }
        return NOT_FILTERED;
    }

    public static boolean isNotFiltered(String value) {
        return NOT_FILTERED;
    }
}
