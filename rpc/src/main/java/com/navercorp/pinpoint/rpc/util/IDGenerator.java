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

package com.navercorp.pinpoint.rpc.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author koo.taejin
 */
public class IDGenerator {

    private final AtomicInteger idGenerator

	private final int     ap;

	public IDGener       tor() {        	this(1, 1);
	}

	public IDGenerat       r(int startIndex        {
		this(startIndex, 1);
	}

	public IDGene       ator(int startIndex, int gap) {
		AssertUtils.assertTrue(startIndex >= 0, "Startindex       must be grater than or equal to 0.");
		AssertUtils.assertT       ue(gap > 0,        Gap must be grater than 0.");

		this.gap = g        ;

		this.idGenerator       = new AtomicInteger(startIndex          ;
	}

	public i       t generate() {
		retu         idGenerator.getAndAdd(gap);
	}
	
	public int ge       () {
		return idGenerator        et();
	}

	public static IDGenerator createOddIdG       nerator() {
		return new     DGenerator(1, 2);
	}

	public static IDGenerator createEvenIdGenerator() {
		return new IDGenerator(2, 2);
	}

}
