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

package com.navercorp.pinpoint.rpc.control;

/**
 * @author koo.taejin
 */
public class ControlMessageProtocolConstant {

    public static final int TYPE_CHARACTER_NULL = 'N';

    public static final int TYPE_CHARACTER_BOOL_TRUE = 'T';

    public static final int TYPE_CHARACTER_BOOL_FALSE = 'F';

    public static final int TYPE_CHARACTER_INT = 'I';

    public static final int TYPE_CHARACTER_LONG = 'L';

    public static final int TYPE_CHARACTER_DOUBLE = 'D';

    public static final int TYPE_CHARACTER_STRING = 'S';

    public static final int CONTROL_CHARACTER_LIST_START = 'V';

    public static final int CONTROL_CHARACTER_LIST_END = 'z';

    public static final int CONTROL_CHARACTER_MAP_START = 'M';

    public static final int CONTROL_CHARACTER_MAP_END = 'z';

}
