/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.parallel;

/**
 * @author HyunGil Jeong
 */
public class ScanTaskException extends RuntimeException {

    private static final long serialVersionUID = 8554224683436066023L;

    public ScanTaskException(Throwable th) {
        super(th);
    }

    public ScanTaskException(String message, Throwable th) {
        super(message, th);
    }
}
