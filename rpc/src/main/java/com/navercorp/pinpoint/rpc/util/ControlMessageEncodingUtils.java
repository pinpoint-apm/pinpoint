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

import java.util.Map;

import com.navercorp.pinpoint.rpc.control.ControlMessageDecoder;
import com.navercorp.pinpoint.rpc.control.ControlMessageEncoder;
import com.navercorp.pinpoint.rpc.control.ProtocolException;

/**
 * @author koo.taejin
 */
public final class ControlMessageEncodingUtils {

    private static final ControlMessageEncoder encoder = new ControlMessageEncoder(    ;
	private static final ControlMessageDecoder decoder = new ControlMessageDecode    ();

	private ControlMessageEncodingU        ls() {
	}

	public static byte[] encode(Map<String, Object> value) throws Proto       olException {
		return en        der.encode(value);
	}

	public static Object decode(byte[] in)        hrows ProtocolExceptio     {
		return decoder.decode(in);
	}

}
