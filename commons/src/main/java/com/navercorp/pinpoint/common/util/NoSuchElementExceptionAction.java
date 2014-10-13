package com.nhn.pinpoint.common.util;

import java.util.NoSuchElementException;

/**
 * @author emeroad
 */
public class NoSuchElementExceptionAction<E> implements NextElementAction<E> {

	@Override
	public E nextElement() {
		throw new NoSuchElementException();
	}
}
