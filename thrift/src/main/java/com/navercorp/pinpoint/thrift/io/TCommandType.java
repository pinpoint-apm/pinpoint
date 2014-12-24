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

package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;

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
	TRANSFER((short) 700, TCommandTransfer.class) {
		@Override
		public TBase newObject() {
			return new TCommandTransfer();
		}
	}, 
	ECHO((short) 710, TCommandEcho.class) {
		@Override
		public TBase newObject() {
			return new TCommandEcho();
		}
	}, 
	THREAD_DUMP((short) 720, TCommandThreadDump.class) {
		@Override
		public TBase newObject() {
			return new TCommandThreadDump();
		}
	},
	THREAD_DUMP_RESPONSE((short) 721, TCommandThreadDumpResponse.class) {
		@Override
		public TBase newObject() {
			return new TCommandThreadDumpResponse();
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
