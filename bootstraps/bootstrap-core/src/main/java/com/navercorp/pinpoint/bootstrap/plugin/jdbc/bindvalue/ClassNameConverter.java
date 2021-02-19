/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author emeroad
 */
public class ClassNameConverter implements Converter {
    @Override
    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            return StringUtils.abbreviate(getClassName(args[1]));
        } else if(args.length == 3) {
           // need to handle 3rd arg?
            return StringUtils.abbreviate(getClassName(args[1]));
        }
        return "error";
    }

    private String getClassName(Object args) {
        if (args == null) {
            return "null";
        }
        return args.getClass().getName();
    }
}
