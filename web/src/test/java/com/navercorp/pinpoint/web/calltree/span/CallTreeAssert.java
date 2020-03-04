/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author jaehong.kim
 */
public class CallTreeAssert {

    public static void assertDepth(final String name, CallTree tree, List<String> result) {
        int index = 0;
        CallTreeIterator iterator = tree.iterator();
        CallTreeNode node = null;
        while ((node = iterator.next()) != null) {
            String depth = getDepthString(node);
            final String expected = result.get(index++);
            assertDepth(name, index, depth, expected);
        }
        if (index != result.size()) {
            fail("Not Matched " + name + " expected is more");
        }
    }

    private static void assertDepth(final String name, final int index, final String depth, final String expected) {
        if (!expected.equals(depth)) {
            fail("Not Matched " + name + "(" + index + "): " + depth + ", expected=" + expected);
        }
    }

    public static void printDepth(CallTree tree) {
        CallTreeIterator iterator = tree.iterator();
        CallTreeNode node = null;
        while ((node = iterator.next()) != null) {
            String depth = getDepthString(node);
            System.out.println(depth + " - " + node.getDepth());
        }
    }

    private static String getDepthString(CallTreeNode node) {
        return StringUtils.repeat('#', node.getDepth() +1);
    }
}