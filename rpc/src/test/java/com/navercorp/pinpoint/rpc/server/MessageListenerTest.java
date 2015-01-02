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

package com.navercorp.pinpoint.rpc.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.navercorp.pinpoint.rpc.server.SimpleLoggingServerMessageListener;

public class MessageListenerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	    Test
	public void serverMessageListenerTest1() throws InterruptedExc       ption {
		PinpointServerSocket ss = new PinpointS       rverSocket();
		ss.bind(       127.0.0.1", 10234);
		ss.setMessageListen       r(new SimpleListener());

		PinpointSocketFactory socketFactory1         createPinpointSocketFactory();
		socketFactory1.setMessag       Listener(new EchoMessageListener());

		PinpointSocketFactory sock       tF          ctory2 = createPinpointSocketFactory();

		try {

			Pinpoint          ocket socket = socketFactory1.connect("127.0.0.1", 10234);
			P          npointSocket           ocket2 = socketFactory2.connect("127.0.0.1", 10234);

			Thread.sleep(500);

	          	List<ChannelContext> channelCo             textLi                   t = ss          getDuplexC       mmunica          ionChannelContext()
			if (channelCont                                tL    st.size() != 2) {
				Assert.fail();
			}

			socket.close();
			soc       et2.close();
		} finally {
			socketFactory1.rele       se();
			socketFactory2.release();
			
			ss.close();
		}
	}

	@Test
	public v       id serverMessageListenerTest2() throws InterruptedException {
		P       npointServerSocket ss = new PinpointServerSocket();
		ss.bind("1       7.0.0.1", 10234);
        ss.setMessageListener(new       Si          pleListener());

		EchoMessageListener echoMessageListener =          new EchoMessa          eListener();

		PinpointSocketFactory socketFactory = createPinpointSocketFact          ry();
		socketFactory.setMessag             Listen                   r(echoMessageListener);

		try {

			PinpointSock                   t socket = socketFactory.connect("127.0.0.1", 10234);
		          Thread.sleep                   500);

			List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
			                   f (channelContextList.size() != 1) {
				Assert.fail();
			}

			ChannelContext channelContext =           hannelCon          extList.get(0);
			
			channelContext.g          tSocketChannel().sendMessage("simple".getBytes());
			Threa          .sleep(100);
			
			Assert.assertEquals("simple", new String(echoMessageListener.getSendPacketRepository().                   et(0       .getPay          oad()));
			
			Fu          ure<R             sp    nseMessage> future = channelContext.getSocketChannel().sendRequestMe       sage("request".getBytes());
			future.await();
		       ResponseMessage message = future.getResult();
			Assert.assertEquals("request"        new String(message.getMessage()));
			Assert.assertEquals("reque       t", new String(echoMessageListener.getRequestPacketRepository().g       t(0).getPayload()));
			
			socket.close();
		} fina             ly {
			socketFactory.release();
			ss.close();
		}
	}

	@Test       	public void serverMessageListenerTest3() throws InterruptedExcep       ion {
		PinpointServerSocket ss = new PinpointServer                      ocket();
		ss.bind("127.0.0.1", 10234);
        ss.setMessage          istener(new SimpleListener());

		PinpointSocketFactory socket                   actory1            createPinpointSocketFactory();
		EchoMessageListener echoMessageListener1 = n          w EchoMessageListener();
		sock             tFacto                   y1.setMessageListener(echoMessageListener1);
		
	          PinpointSocketFactory socketFactory2 = createPinpointSocketFactory();
		EchoMessageListener echoMessag          Listener2 = new EchoMessageListener();
		socketFactor          2.setMessageListener(echoMessageListener2);
		
		try {

			PinpointSocket socket = socketFactory1.connect          "127.0.0.          ", 10234);          			PinpointSocket socket2 = socketFactory2.connect("127.0.0.1", 10234)
			
			Thread.sleep(500);

			List<ChannelContext> channelContextList                     ss.          etDuplexCo       municat          onChannelContext();          			if (channelConte                                       ist.size() != 2) {
				Assert.fail();
			}

			ChannelContext channe       Context = channelContextList.get(0);
			Future<Re       ponseMessage> future = channelContext.getSocketChannel().sendRequestMessage("s       cket1".getBytes());
	       	ChannelContext channelContext2 = channelContextList.get(1);
			Future       ResponseMessage> future2 = channelContext2.getSocketChann       l                   ).sendRequestMessage("socket2".getBytes());

			future.                   wait();
          		future2.await();
			Assert.assertEquals("socket1", new String(future.getResult().getMessage()));
			Assert.assertEquals("socket2", new String(future2.getResult().getMessage()));
			
          		socket.close();
			socket2.clo          e();
		} finally {
			socketFactory1.release();
			socketFactory2.release();
			
			ss.close();
		}
	}
	
	@Test
	public void serverMessageListenerTest4() throws InterruptedE          ception {
		PinpointServerSoc          et ss = n       w Pinpo          ntServerSocket();
          	ss.b             nd    "127.0.0.1", 10234);
        ss.setMessageListener(new SimpleListene       ());

		Map params = getParams();
		PinpointSocke       Factory socketFactory = createPinpointSocketFactory(params);
		socketFactory.s       tMessageListener(new EchoMessageListener());

		try {
			
			Pin       ointSocket socket = socketFactory.connect("127.0.0.1", 10234);
			
	       	          hread.sleep(500);

			ChannelContext channelContext = getChannelContext("application          , "agent", (Long) params.get(AgentHandshakePropertyType.START          TIMESTAMP.get          ame()), ss.getDuplexCommunicationChannelContext());
			Assert.assertNotNull(ch          nnelContext);

			channelContex              = get                   hannel       ontext(          application", "age          t", (             ong) params.get(AgentHandshakePropertyType.START_TIMESTAMP.getN    me(    ) + 1, ss.getDuplexCommunicationChannelContext());
			Assert.assertN       ll(channelContext);

			socket.close();
		} finally {
			so             ketFactory.release();
			ss.close();
		}
	}

	       Test
	public void server       essageListenerTest5() throws Interr       ptedException {
		PinpointServerSocket ss = new PinpointServerSo       ket();
		ss.bind("127.0.0.1", 10234);
              ss.setMessageListener(new SimpleListener());

		Pinpo                      ntSocketFactory socketFactory = createPinpointSocketFactory();
		socketFactory.setMe          sageListener(SimpleLoggingMessageListener.LISTENER);
		try {          			// SimpleLo          gingMessageListener.LISTENER as default listener can't connect by duplex mode.          			PinpointSocket socket = sock             tFacto                            y.connect("127.0.0.1", 10234);

			Thread.sleep(500);

	          	List<ChannelContext> channelContextList = ss.getDuplex             ommuni                   ationC       annelCo          text();
			if (cha          nelCo                textList.size() != 1) {
				Assert.fail();
			}

			sock       t.close();
		} finally {
			socketFactory.re        ase();
			ss.close();
		}
	}

	// confirm how many times retry when        acket has not been received.
	@Test
	public void serverMessageListener       est6() throws InterruptedException {
		D       plexCheckListener serverL        tener = new DuplexCheck       istener();
		
		PinpointServerSocket ss = new PinpointServerSocket();
		ss.bind("127.0.0.1", 10234);
		ss.setMessageListener(serverListener);

		PinpointSocketFactory socketFactory = createPinpointSocketFactory();
		socketFactory.setEnableWorkerPacketDelay(500);
		socketFactory.setMessageListener(new EchoMessageListener());
		
		try {

			// SimpleLoggingMessageListener.LISTENER as default listener can't connect by duplex mode.
			PinpointSocket socket = socketFactory.connect("127.0.0.1", 10234);
			Thread.sleep(5000);

			List<ChannelContext> channelContextList = ss.getDuplexCommunicationChannelContext();
			if (channelContextList.size() != 0) {
				Assert.fail();
			}
			
			System.out.pr       ntln(serverLis        ner.getReceiveEnableWorkerPacketCount());
			if (ser       erListener.getReceiveEnableWorkerPacketCount() < 8) {
				Assert.fail();
			}
       			socket.close();
		} finally {
			socketFactory.release();
			ss.close();
		}
	}
	
	pr       vate        inpointSocketFactory createPinpointSocketFactory() {
		retur           createPinpointSocketFactory(ge                   Params());
	}

	private PinpointSocketFactory createPinpointSocketFactory(Map             para       ) {
		PinpointSocketFactory pinpointSocketFactory = new PinpointSocke          Factory();
		pinpointSocketFactory.se                   Properties(param);

		return pinp          intSocketFactory;
	}

	privat           Map getParams() {
		Map properties = new HashMap();

        properties.put(AgentHand             hakePropertyType.AGENT_ID.getName(), "agent");                  properties.put             AgentHandshakePropertyType.APPLICATION_NAME.getName(          , "application");
                       properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "ho             tname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "             p"       ;
        properties.put(AgentHandshakePropertyType.PID.ge          Name(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        pro             erties.put(AgentHandshakePropertyType.START          TIMESTAMP.getName(), System.currentTim                      Millis());
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0");
        
		return properties;
	}

	class EchoMessageListene     implements MessageListener {
		       rivate final    Lis    <S    ndPacket> sendPacketRepo       itory = new     rra    Li    t<SendPacket>();
		private fi       al List<Requ    stP    ck    t> requestPacketRepository = new ArrayList<RequestPacket>();

		@Override
		p    blic void handleSend(SendPacket sendPacket, Channel channel) {
			sendPacketRepository.add(sendPacket);
			
			byte[] payload = sendPacket.getPayload();
            logger.debug(new String(payload));
		}

		@Override
		public void handleRequest(RequestPacket requestPacket, Channel channel) {
			requestPacketRepository.add(requestPacket);
			
			byte[] payload = requestPacket.getPayload();
			logger.debug(new String(payload));

			channel.write(new ResponsePacket(requestPacket.getRequestId(), requestPacket.getPayload()));
		}

		public List<SendPacket> getSendPacketRepository() {
			return sendPacketRepository;
		}

		public List<RequestPacket> getRequestPacketRepository() {
			return requestPacketRepository;
		}
	}
	
	class DuplexCheckListener extends SimpleLoggingServerMessageListener {
		
		private fina     At    mic    nteger receiveEnableWorkerPacketCount =       new AtomicIn    eger    );
		@Override
		public HandshakeRespon       eCode handleHandshake(Map propert    es) {
			r       ceiveEnableWorkerPacketCount.incrementAndGet();
            return HandshakeResponseType.Error.UNKNOWN_ERROR;
		}

		public int getReceive       nableWorkerP    cketC       unt() {
			return receiveEnableWorkerPacketCount.get();
		}
		
	}
	
	private ChannelContext getChannelContext(String applicationName, String agentId, long startTimeMillis, List<ChannelContext> duplexChannelContextList) {
    	if (applicationName == null) {
    		return null;
    	}
    	
    	if (agentI     =        null) {
    		return null;
    	}
    	
    	if (startTimeMillis <= 0) {
    		return null;
    	}
    	
    	List<ChannelContext> channelContextList = new ArrayList<ChannelContext>();

    	for (ChannelContext eachContext : duplexChannelContextList) {
            if (eachContext.getCurrentStateCode() == PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION) {
                Map agentProperties = eachContext.getChannelProperties();

                if (!applicationName.equals(agentProperties.get(AgentHandshakePropertyType.APPLICATION_NAME.getName()))) {
                    continue;
                }

                if (!agentId.equals(agentProperties.get(AgentHandshakePropertyType.AGENT_ID.getName()))) {
                    continue;
                }

                if (startTimeMillis != (Long) agentProperties.get(AgentHandshakePropertyType.START_TIMESTAMP.getName())) {
                    continue;
                }

                channelContextList.add(eachContext);
            }
    	}
    	

    	if (channelContextList.size() == 0) {
    		return null;
    	} 
    	
    	if (channelContextList.size() == 1) {
    		return channelContextList.get(0);
    	} else {
    		logger.warn("Ambiguous Channel Context {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeMillis, channelContextList);
    		return null;
    	}
    }
	
	private class SimpleListener extends SimpleLoggingServerMessageListener {

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.info("handleEnableWorker {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;

        }
	    
	}
	

}
