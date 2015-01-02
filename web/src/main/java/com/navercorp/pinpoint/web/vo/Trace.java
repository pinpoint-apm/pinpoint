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

package com.navercorp.pinpoint.web.vo;

/**
 * @author emeroad
 */
public class Trace {

    private final String transactionI    ;
	private final long execution    ime;
	private final long sta    tTime;

	private final int exce    tionCode;

	public Trace(String transactionId, long executionTime, long startTime, int exceptionCode) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        this.transactionId =        ransactionId;
		this.executionT       me = executionTime;
		t       is.startTime = startTime;
		thi        exceptionCode = exceptionCode;


	public String         tTransactionId() {
		return tr       nsactionId;
	}

	        blic long getExecutionTime       ) {
		return         ecutionTime;
	}

	public long       getStartTime() {
    	return startTime;
	}

	public int getExceptionCode() {
		return exceptionCode;
	}
}
