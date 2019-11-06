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

package com.navercorp.pinpoint.common.hbase.namespace;

import org.apache.hadoop.hbase.TableName;

import java.util.regex.Pattern;

/**
 * @author HyunGil Jeong
 */
public class HbaseNamespaceValidator implements NamespaceValidator {

    public static final NamespaceValidator INSTANCE = new HbaseNamespaceValidator();

    private static final Pattern VALID_NAMESPACE = Pattern.compile(TableName.VALID_NAMESPACE_REGEX);

    private HbaseNamespaceValidator() {

    }

    @Override
    public boolean validate(String namespace) {
        if (namespace == null) {
            return false;
        }
        return VALID_NAMESPACE.matcher(namespace).matches();
    }
}
