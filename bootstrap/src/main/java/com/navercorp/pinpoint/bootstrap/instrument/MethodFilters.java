/**
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
package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author Jongho Moon
 *
 */
public class MethodFilters {

    private MethodFilters() { }
    
    public static MethodFilter name(String... names) {
        return new MethodNameFilter(names);
    }
    
    public static MethodFilter modifier(int required) {
        return modifier(required, 0);
    }
    
    public static MethodFilter modifierNot(int rejected) {
        return modifier(0, rejected);
    }
    
    public static MethodFilter modifier(int required, int rejected) {
        return new ModifierFilter(required, rejected);
    }
    
    
    private static final class MethodNameFilter implements MethodFilter {
        private final String[] names;

        public MethodNameFilter(String[] names) {
            this.names = names;
        }

        @Override
        public boolean filter(MethodInfo method) {
            for (String name : names) {
                if (name.equals(method.getName())) {
                    return false;
                }
            }

            return true;
        }
    }
    
    private static final class ModifierFilter implements MethodFilter {
        private final int required;
        private final int rejected;
        
        public ModifierFilter(int required, int rejected) {
            this.required = required;
            this.rejected = rejected;
        }

        @Override
        public boolean filter(MethodInfo method) {
            int modifier = method.getModifiers();
            return ((required & modifier) != required) || ((rejected & modifier) != 0);
        }
    }
}
