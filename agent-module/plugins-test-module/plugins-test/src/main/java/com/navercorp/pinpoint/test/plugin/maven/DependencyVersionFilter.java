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

    private static final Pattern RC_PATTERN = Pattern.compile(".*[Rr][Cc]-?\\.?\\d*$");
    private static final Pattern M_PATTERN = Pattern.compile(".*[Mm]-?\\.?\\d*$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile(".*[Aa][Ll][Pp][Hh][Aa]-?\\.?\\d*$");
    private static final Pattern BETA_PATTERN = Pattern.compile(".*[Bb][Ee][Tt][Aa]-?\\.?\\d*$");
    private static final Pattern PATCH_PATTERN = Pattern.compile(".*[Pp][Aa][Tt][Cc][Hh]-?\\.?\\d*$");
    private static final Pattern TEST_PATTERN = Pattern.compile(".*[Tt][Ee][Ss][Tt]-?\\.?\\d*$");
    private static final Pattern MILESTONE_PATTERN = Pattern.compile(".*[Mm][Ii][Ll][Ee][Ss][Tt][Oo][Nn][Ee]-?\\.?\\d*$");
    private static final Pattern PRE_PATTERN = Pattern.compile(".*[Pp][Rr][Ee]-?\\.?\\d*$");

    private static final Pattern[] PATTERNS = new Pattern[]{RC_PATTERN, M_PATTERN, ALPHA_PATTERN, BETA_PATTERN, PATCH_PATTERN, TEST_PATTERN, MILESTONE_PATTERN, PRE_PATTERN};


    @Override
    public boolean test(String value) {
        for (Pattern pattern : PATTERNS) {
            if (pattern.matcher(value).matches()) {
                return FILTERED;
            }
        }
        return NOT_FILTERED;
    }

    public static boolean isNotFiltered(String value) {
        return NOT_FILTERED;
    }
}
