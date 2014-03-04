package com.nhncorp.lucy.nimm.connector.agent;

import com.nhncorp.lucy.nimm.connector.address.NimmAddress;

interface ServiceAgentAPI {


	void sendMessage(NimmAddress targetAddress, byte[] message);

}
