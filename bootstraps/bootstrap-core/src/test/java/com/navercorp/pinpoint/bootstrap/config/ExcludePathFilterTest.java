/*
 * Copyright 2016 Naver Corp.
 *
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

package com.navercorp.pinpoint.bootstrap.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.navercorp.pinpoint.bootstrap.config.Filter.FILTERED;
import static com.navercorp.pinpoint.bootstrap.config.Filter.NOT_FILTERED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HyunGil Jeong
 */
public class ExcludePathFilterTest {

    @Test
    public void testNotFiltered() {
        Filter<String> filter = new ExcludePathFilter("/exclude/*");
        assertNotFiltered(filter, null);
        assertNotFiltered(filter, "");
        assertNotFiltered(filter, "/");
        assertNotFiltered(filter, "a");
        assertNotFiltered(filter, "/include");
        assertNotFiltered(filter, "/include/a");
        assertNotFiltered(filter, "/exclude");
        assertNotFiltered(filter, "/exclude/a/b");
    }

    @Test
    public void testExactMatchFiltering() {
        Filter<String> filter = new ExcludePathFilter("/a/b/c");
        assertFiltered(filter, "/a/b/c");

        assertNotFiltered(filter, "a/b/c");
        assertNotFiltered(filter, "/a/b/c/");
    }

    @Test
    public void testWildcardFiltering() {
        Filter<String> filter = new ExcludePathFilter("a?c, d??f");
        assertFiltered(filter, "abc");
        assertFiltered(filter, "dabf");

        assertNotFiltered(filter, "ac");
        assertNotFiltered(filter, "def");
    }

    @Test
    public void testSingleLevelFiltering() {
        Filter<String> filter = new ExcludePathFilter("/exclude/*, /*/exclude, /exclude/*/this");
        assertFiltered(filter, "/exclude/");
        assertFiltered(filter, "/exclude/a");
        assertFiltered(filter, "/a/exclude");
        assertFiltered(filter, "/exclude/a/this");

        assertNotFiltered(filter, "exclude");
        assertNotFiltered(filter, "/exclude");
        assertNotFiltered(filter, "/exclude/a/b");
        assertNotFiltered(filter, "a/exclude");
        assertNotFiltered(filter, "/a/b/exclude");
        assertNotFiltered(filter, "/exclude/a/b/this");
    }

    @Test
    public void testMultipleLevelFiltering() {
        Filter<String> filter = new ExcludePathFilter("/exclude/**, /**/exclude, /something/**/else");
        assertFiltered(filter, "/exclude/a");
        assertFiltered(filter, "/exclude/a/b");
        assertFiltered(filter, "/a/exclude");
        assertFiltered(filter, "/a/b/exclude");
        assertFiltered(filter, "/something/else");
        assertFiltered(filter, "/something/a/else");
        assertFiltered(filter, "/something/a/b/else");

        assertNotFiltered(filter, "a/exclude");
        assertNotFiltered(filter, "something/else");
    }

    private void assertFiltered(Filter<String> filter, String testValue) {
        assertThat(filter.filter(testValue)).isEqualTo(FILTERED);
    }

    private void assertNotFiltered(Filter<String> filter, String testValue) {
        assertThat(filter.filter(testValue)).isEqualTo(NOT_FILTERED);
    }

    // Tests for urls
    @Test
    public void testFilter() {
        Filter<String> filter = new ExcludePathFilter("/monitor/l7check.html, test/l4check.html");

        assertFilter(filter);
    }

    @Test
    public void testFilter_InvalidExcludeURL() {
        Filter<String> filter = new ExcludePathFilter("/monitor/l7check.html, test/l4check.html, ,,");

        assertFilter(filter);
    }

    @Test
    public void testFilter_emptyExcludeURL() {
        Filter<String> filter = new ExcludePathFilter("");

        Assertions.assertFalse(filter.filter("/monitor/l7check.html"));
        Assertions.assertFalse(filter.filter("test/l4check.html"));

        Assertions.assertFalse(filter.filter("test/"));
        Assertions.assertFalse(filter.filter("test/l4check.htm"));
    }

    private void assertFilter(Filter<String> filter) {
        Assertions.assertTrue(filter.filter("/monitor/l7check.html"));
        Assertions.assertTrue(filter.filter("test/l4check.html"));

        Assertions.assertFalse(filter.filter("test/"));
        Assertions.assertFalse(filter.filter("test/l4check.htm"));

        Assertions.assertFalse(filter.filter(null));
        Assertions.assertFalse(filter.filter(""));
    }

    @Test
    public void antStylePath() {
        Filter<String> filter = new ExcludePathFilter("/monitor/l7check.*,/*/l7check.*");

        Assertions.assertTrue(filter.filter("/monitor/l7check.jsp"));
        Assertions.assertTrue(filter.filter("/monitor/l7check.html"));

        Assertions.assertFalse(filter.filter("/monitor/test.jsp"));

        Assertions.assertTrue(filter.filter("/*/l7check.html"));

        Assertions.assertFalse(filter.filter(null));
        Assertions.assertFalse(filter.filter(""));
    }

    @Test
    public void antstyle_equals_match() {
        Filter<String> filter = new ExcludePathFilter("/monitor/stringEquals,/monitor/antstyle.*");

        Assertions.assertTrue(filter.filter("/monitor/stringEquals"));
        Assertions.assertTrue(filter.filter("/monitor/antstyle.html"));

        Assertions.assertFalse(filter.filter("/monitor/stringEquals.test"));
        Assertions.assertFalse(filter.filter("/monitor/antstyleXXX.html"));
    }
}
