package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;

import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDump;

/**
 * @author koo.taejin
 */
public enum TCommandType {

	// 그냥 Reflection으로 하는게 훨씬 깔끔한데 고민끝에 이렇게 함 
	// 예외 처리, 생성자 문제, Registry에서 성능 문제 등등 그냥 코드좀 더럽게 함

	RESULT((short) 320, TResult.class) {
		@Override
		public TBase newObject() {
			return new TResult();
		}
	}, 
	THREAD_DUMP((short) 720, TCommandThreadDump.class) {
		@Override
		public TBase newObject() {
			return new TCommandThreadDump();
		}
	};

	private final short type;
	private final Class<? extends TBase> clazz;
	private final Header header;

	private TCommandType(short type, Class<? extends TBase> clazz) {
		this.type = type;
		this.clazz = clazz;
		this.header = createHeader(type);
	}

	protected short getType() {
		return type;
	}
	
	protected Class getClazz() {
		return clazz;
	}

	protected boolean isInstanceOf(Object value) {
		return this.clazz.isInstance(value);
	}
	
	protected Header getHeader() {
		return header;
	}
	
	// 그냥 Reflection으로 하는게 훨씬 깔끔한데 고민끝에 이렇게 함 
	// 예외 처리, 생성자 문제, Registry에서 성능 문제 등등 그냥 코드좀 더럽게 함
	public abstract TBase newObject();
	
	private static Header createHeader(short type) {
		Header header = new Header();
		header.setType(type);
		return header;
	}

}
