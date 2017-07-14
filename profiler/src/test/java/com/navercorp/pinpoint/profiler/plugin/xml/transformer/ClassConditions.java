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
package com.navercorp.pinpoint.profiler.plugin.xml.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;

/**
 * @author Jongho Moon
 *
 */
public class ClassConditions {

    public static ClassCondition hasField(String name) {
        return new HasField(name);
    }

    public static ClassCondition hasField(String name, String type) {
        return new HasField(name, type);
    }

    public static ClassCondition hasMethod(String name, String... paramTypes) {
        return new HasMethod(name, paramTypes);
    }

    public static ClassCondition hasDeclaredMethod(String name, String... paramTypes) {
        return new HasDeclaredMethod(name, paramTypes);
    }

    public static ClassCondition hasConstructor(String... paramTypes) {
        return new HasConstructor(paramTypes);
    }

    public static ClassCondition hasClass(String name) {
        return new HasClass(name);
    }

    public static ClassCondition hasNotDeclaredMethod(String name, String... paramTypes) {
        return new HasNotDeclaredMethod(name, paramTypes);
    }

    private static class HasField implements ClassCondition {
        private final String name;
        private final String type;

        public HasField(String name) {
            this(name, null);
        }

        public HasField(String name, String type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean check(InstrumentContext context, ClassLoader classLoader, InstrumentClass target) {
            return target.hasField(name, type);
        }

        @Override
        public String toString() {
            return "HasField[" + type + " " + name + "]";
        }
    }

    private static class HasMethod implements ClassCondition {
        private final String name;
        private final String[] paramTypes;

        public HasMethod(String name, String... paramTypes) {
            this.name = name;
            this.paramTypes = paramTypes;
        }

        @Override
        public boolean check(InstrumentContext context, ClassLoader classLoader, InstrumentClass target) {
            return target.hasMethod(name, paramTypes);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("HasMethod[");
            builder.append(name);
            builder.append('(');

            for (String p : paramTypes) {
                builder.append(p);
            }

            builder.append(")]");

            return builder.toString();
        }
    }

    private static class HasDeclaredMethod implements ClassCondition {
        private final String name;
        private final String[] paramTypes;

        public HasDeclaredMethod(String name, String[] paramTypes) {
            this.name = name;
            this.paramTypes = paramTypes;
        }

        @Override
        public boolean check(InstrumentContext context, ClassLoader classLoader, InstrumentClass target) {
            return target.hasDeclaredMethod(name, paramTypes);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("HasMethod[");
            builder.append(name);
            builder.append('(');

            for (String p : paramTypes) {
                builder.append(p);
            }

            builder.append(")]");

            return builder.toString();
        }
    }
    
    private static class HasNotDeclaredMethod extends HasDeclaredMethod {
        
        public HasNotDeclaredMethod(String name, String[] paramTypes) {
            super(name, paramTypes);
        }

        @Override
        public boolean check(InstrumentContext context, ClassLoader classLoader, InstrumentClass target) {
            return !super.check(context, classLoader, target);
        }
    }

    private static class HasConstructor implements ClassCondition {
        private final String[] paramTypes;

        public HasConstructor(String[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        @Override
        public boolean check(InstrumentContext context, ClassLoader classLoader, InstrumentClass target) {
            return target.hasConstructor(paramTypes);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("HasConstructor[");

            for (String p : paramTypes) {
                builder.append(p);
            }

            builder.append(")]");

            return builder.toString();
        }
    }

    private static class HasClass implements ClassCondition {
        private final String name;

        public HasClass(String name) {
            this.name = name;
        }

        @Override
        public boolean check(InstrumentContext context, ClassLoader classLoader, InstrumentClass target) {
            return context.exist(classLoader, name);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{name=");
            builder.append(name);
            builder.append("}");
            return builder.toString();
        }
    }

}
