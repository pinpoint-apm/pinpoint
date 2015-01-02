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
public class DelegateEnumeration<E> implements Enumeration<E> {
    private static final Object NULL_OBJECT = new Object()

	private final Enumeration<E> dele    ate;
	private final Filter<E>     ilter;

	private boolean hasMo    eElements;
	private      nextElement;
	private Exception    nextException;


	private static final Filter SKIP_FIL       ER =        ew Filter() {
		@Override
		p          blic bo             lean filter(Object o) {
			r    turn false;
		}
	};

	@SuppressWarnings("unchecked"
	public DelegateEnumera        on(Enumeration<E> delegate) {
		this(delegate, SKIP_FILTER);
	}

	pub       ic DelegateEnumeratio       (Enumeration<E> d        egate,     ilter<E> filter) {
		this.delega       e =       delegate;
		this.fi        er = fi    ter;
	}

	@Override
	p       bli        boolean hasMoreElements          ) {
		next();
		return hasMoreEleme          ts;
	}

	@Override
	          ublic E nextElement() {
		next();
		if (nex             Exception != null) {
			Exce       tion exception = thi       .nextExcep        on;
			this.nextException         null;
			this.<RuntimeExcept          on>thr             wException(ex        ption);
		}
		final E result    = getNextElement();
		this.nextElement = null;
		return result;
	}

	private E        etNextElement()
    	if (nextElement ==       NULL_OBJECT) {
			return null;
		}
		return nex          E             ement;
	}
	@SuppressWarnings("unchecked")
	private <T extends           xception                       void throwException(Exceptio           exception) throw              T {
		throw (T) exception;
	}

	private void                                ext() {
		if (nextEleme             t                   != null || nextException != nu          l) {
			return;
		}
             		while (true) {
			final b          ol             an hasMoreElements = de                               egate.hasMoreElements();
			E ne       tElement;
			try    {
				nextElement = delegate.nextElement();
			} catch (Exception e) {
				this.hasMoreElements = hasMoreElements;
				this.nextException = e;
				break;
			}

			if (filter.filter(nextElement)) {
				continue;
			}

			this.hasMoreElements = hasMoreElements;
			if (nextElement == null) {
				this.nextElement = (E) NULL_OBJECT;
			} else {
				this.nextElement = nextElement;
			}
			break;

		}

	}


	public static interface Filter<E> {
		boolean filter(E e);
	}
}
