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

package com.navercorp.pinpoint.rpc.control;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class ControlMessageDecoder {

    private Charset charset

	public ControlMessageDecode       () {
		this.charset = Charset.forNam        "UTF-8");
	}

	public Object decode(byte[] in) throws Pr       tocolException {
		return decod             (ByteBuffer.wrap(in));
	}
		
	public Object decode(ByteBu       fer in) throws Pr       tocolExcept       on {
		byte type = in.get();
		switch (type) {
		cas           Contr       lMessageProtocolConstant.TYPE_CHARACTER_NULL:
			return n          ll;
		case Con       rolMessageProtocolConstant.TYPE_CHARACTER_BOOL_TRUE:
			re          urn Boolean.TRU       ;
		case ControlMessageProtocolConstant.TYPE_CHARAC          ER_BOOL_FALSE
			return Boolean.FALSE;
		case ControlMessageProto          olConstant.TYP       _CHARACTER_INT:
			return in.getInt();
		case ControlM          ssageProtocolConstant.TYPE_CHARACTER_LO       G:
			return in.getLong();
		case ControlMessageProtoc          lConstant.TYPE_CHA       ACTER_DOUBLE:
			return Double.longBitsToDouble(in.getLong())
		case ControlMessageProtocolConstant.TYPE_          HARACTER_STRING:
			ret             rn decodeString(in)
		case ControlMessage          rotocolConst       nt.CONTROL_CHARACTER_LIST_START:
			List<Object> answerList            new ArrayList<Object>();
			while (!isListFinished(in)) {
			          answerList.add(decode(             n));
			}
			in.             et(); // Skip the              erminator
			retur                    answerList;
		case Co          trolMessage       roto          olConstant.CONTROL_CHARACTER_MAP_START:
			Map<Object, Object> answerMap = new LinkedHashMap<Object, Object>();
             		while (!isMapFinished(in)) {
				Object       key = decode(in);
				Object va       ue = decode(in);
				answerMap.put(k       y, value);
			}
			       n.get(); // Skip the terminator
			ret        n answerMap;
		default:
			throw new Protoco       Exception("invalid type character: " + (char) type + " (" + "0x" + Integer.toHexStrin        type) + ")");
		}
	}

	private Object decodeS       ring(ByteBuffer in) {
		int length = readStringLength(in);

		byte[] bytesToEncode = n         byte[length];
		in.get(bytesToEncode);

		       eturn new S       ring(bytesT       Encode, ch          rset);
	}

	          rivate boolean isMapFini          hed(ByteBuffer i                      ) {
             	return     n.get(in.position()) == ControlMessageProtocolConstant.CONTROL_CHARACTER_MAP_END;
	}

	private boolean isListFinished(ByteBuffer in) {
		return in.get(in.position()) == ControlMessageProtocolConstant.CONTROL_CHARACTER_LIST_END;
	}

	private int readStringLength(ByteBuffer in) {
		int result = 0;
		int shift = 0;

		while (true) {
			byte b = in.get();
			result |= (b & 0x7F) << shift;
			if ((b & 0x80) != 128)
				break;
			shift += 7;
		}
		return result;
	}

}
