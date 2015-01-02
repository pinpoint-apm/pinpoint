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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.util.AssertUtils;

/**
 * @author koo.taejin
 */
public class ClientStreamChannelContext extends StreamChannelContext {

    private final ClientStreamChannel clientStreamChanne    ;
	private final ClientStreamChannelMessageListener clientStreamChannelMessageListe    er;

	public ClientStreamChannelContext(ClientStreamChannel clientStreamChannel, ClientStreamChannelMessageListener clientStreamChannelMessageLis       ener) {
		AssertUtils.assertNotNull(clientS       reamChannel);
		AssertUtils.assertNotNull(clientStreamChann       lMessageListener);

		this.clientStreamChan       el = clientStreamChannel;
		this.clientStreamChannelMessageListener = cli        tStream    hannelMessageListener;
	}

	@Override
	public       ClientStreamChannel get        reamChannel() {
		return clientStreamChannel;
	}

	public ClientStreamChannelMess       geListener getClientStreamChannelMessa    eListener() {
		return clientStreamChannelMessageListener;
	}

}
