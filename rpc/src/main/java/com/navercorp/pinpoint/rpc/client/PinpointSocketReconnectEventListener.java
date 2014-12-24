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

package com.navercorp.pinpoint.rpc.client;

public interface PinpointSocketReconnectEventListener {
	
	// 현재는 Reconnect를 제외한 별다른 Event가 없음 
	// 이후에 별다른 Event가 있을 경우 Event와 함께 넘겨주면 좋을듯함
	void reconnectPerformed(PinpointSocket socket);
	
}
