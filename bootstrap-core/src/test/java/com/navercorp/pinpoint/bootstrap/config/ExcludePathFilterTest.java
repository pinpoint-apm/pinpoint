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

import org.junit.Assert;
import org.junit.Test;

import static com.navercorp.pinpoint.bootstrap.config.Filter.*;
import static org.hamcrest.core.Is.is;

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
        Assert.assertThat(filter.filter(testValue), is(FILTERED));
    }

    private void assertNotFiltered(Filter<String> filter, String testValue) {
        Assert.assertThat(filter.filter(testValue), is(NOT_FILTERED));
    }

    // Tests for urls
    @Test
    public void testFilter() throws Exception {
        Filter<String> filter = new ExcludePathFilter("/monitor/l7check.html, test/l4check.html");

        assertFilter(filter);
    }

    @Test
    public void testFilter_InvalidExcludeURL() throws Exception {
        Filter<String> filter = new ExcludePathFilter("/monitor/l7check.html, test/l4check.html, ,,");

        assertFilter(filter);
    }

    @Test
    public void testFilter_emptyExcludeURL() throws Exception {
        Filter<String> filter = new ExcludePathFilter("");

        Assert.assertFalse(filter.filter("/monitor/l7check.html"));
        Assert.assertFalse(filter.filter("test/l4check.html"));

        Assert.assertFalse(filter.filter("test/"));
        Assert.assertFalse(filter.filter("test/l4check.htm"));
    }

    private void assertFilter(Filter<String> filter) {
        Assert.assertTrue(filter.filter("/monitor/l7check.html"));
        Assert.assertTrue(filter.filter("test/l4check.html"));

        Assert.assertFalse(filter.filter("test/"));
        Assert.assertFalse(filter.filter("test/l4check.htm"));

        Assert.assertFalse(filter.filter(null));
        Assert.assertFalse(filter.filter(""));
    }

    @Test
    public void antStylePath() throws Exception {
        Filter<String> filter = new ExcludePathFilter("/monitor/l7check.*,/*/l7check.*");

        Assert.assertTrue(filter.filter("/monitor/l7check.jsp"));
        Assert.assertTrue(filter.filter("/monitor/l7check.html"));

        Assert.assertFalse(filter.filter("/monitor/test.jsp"));

        Assert.assertTrue(filter.filter("/*/l7check.html"));

        Assert.assertFalse(filter.filter(null));
        Assert.assertFalse(filter.filter(""));
    }

    @Test
    public void antstyle_equals_match() throws Exception {
        Filter<String> filter = new ExcludePathFilter("/monitor/stringEquals,/monitor/antstyle.*");

        Assert.assertTrue(filter.filter("/monitor/stringEquals"));
        Assert.assertTrue(filter.filter("/monitor/antstyle.html"));

        Assert.assertFalse(filter.filter("/monitor/stringEquals.test"));
        Assert.assertFalse(filter.filter("/monitor/antstyleXXX.html"));
    }
}
