/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.test;

/**
 * @author Taejin Koo
 */
public enum ExpectedTraceFieldType {

    EQUALS {
        @Override
        public boolean isEquals(String expected, String actual) {
            if (expected == null && actual == null) {
                return true;
            }
            if (expected == null) {
                return false;
            }
            return expected.equals(actual);
        }
    },

    ALWAYS_TRUE {
        @Override
        public boolean isEquals(String expected, String actual) {
            return true;
        }
    },

    NOT_EMPTY {
        @Override
        public boolean isEquals(String expected, String actual) {
            if (actual == null || actual.length() == 0) {
                return false;
            }
            return true;
        }
    },

    EMPTY {
        @Override
        public boolean isEquals(String expected, String actual) {
            if (actual == null || actual.length() == 0) {
                return true;
            }
            return false;
        }
    },

    START_WITH {
        @Override
        public boolean isEquals(String expected, String actual) {
            if (expected == null && actual == null) {
                return true;
            }
            if (expected == null) {
                return true;
            }
            if (actual != null) {
                return actual.startsWith(expected);
            }
            return false;
        }

    },

    CONTAINS {
        @Override
        public boolean isEquals(String expected, String actual) {
            if (expected == null && actual == null) {
                return true;
            }
            if (expected == null) {
                return true;
            }
            if (actual != null) {
                return actual.contains(expected);
            }
            return false;
        }
    };

    public abstract boolean isEquals(String expected, String actual);

}
