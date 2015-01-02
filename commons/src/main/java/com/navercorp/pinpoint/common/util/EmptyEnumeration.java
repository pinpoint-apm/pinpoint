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

package com.navercorp.pinpoint.common.util;

import java.util.Enumeration;

/**
 * @author emeroad
 */
public class EmptyEnumeration<E> implements Enumeration<E> {
    private static final NullNextElementAction NULL_NEXT_ELEMENT_ACTION = new NullNextElementAction()

	private final NextElementAction<E> nextElementAct    on;

	@SuppressWarnings("unc    ecked")
	public EmptyEnum       ration() {
		this(NULL_NEXT        LEMENT_ACTION);
	}

	public EmptyEnumeration(NextElementAction<       > nextElementAction) {
		this.nextEleme        Action      nextElementAction;
	}

	@Overri       e
	public        oolean     asMoreElements() {
		r       turn false;
	}

	@Override
	public      nextElement() {
		return nextElementAction.nextElement();
	}
}
