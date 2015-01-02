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

    // Using reflection would make code cleaner.
    // But it also makes it hard to handle exception, constructor and will show relatively low performance.

    RESULT((short) 320, TResult.class)       {
		@       verride
		public TBase          newObject() {
	             	return new TResult();
		}
	}, 
	TRANSFER((sh       rt) 7       0, TCommandTransfer.cl          ss) {
		@Override
		publ             c TBase newObject() {
			return new T       omman       Transfer();
		}
	}, 
	          CHO((short) 710, TCo             mandEcho.class) {
		@Override
		public TBase newOb       ect()       {
			return new TComma          dEcho();
		}
	}, 
	THREAD_             UMP((short) 720, TCommandThreadDump.class) {
		@Override
		public        Base        ewObject() {
			return          new TCommandThreadDump();
		}
	},
             THREAD_DUMP_RESPONSE((s    ort) 721, TCommandThreadDumpResponse.clas    ) {
		@Override
		public TB    se newObject() {
			return new TCommandThreadDumpResponse();
	       }
	};

	priva       e final short t       pe;
	private final Class<? ex        nds TBase> clazz;
	privat        final H          ader header;

	private TC       mmandType        hort type, Class<? extends TBase> clazz) {
	       this.type = type;
		this.clazz =          clazz;
		this.header = crea       eHeader(ty          e);
	}

	protected short getTyp       () {
		return type;
	}
	
	protected Class get       lazz() {
		return clazz;
       }

	protected boo       ean isInst    nceOf(Object value) {
		return this.clazz.isInstance(value);
	}
	
	protected Header getHeader() {
		return header;
	}
	
	public abstract TBase newObject();
	
	private static Header createHeader(short type) {
		Header header = new Header();
		header.setType(type);
		return header;
	}

}
