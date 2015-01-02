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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.RecordedStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.TestSeverMessageListener;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelContext;

public class StreamChannelManagerTest {

    // Client to Server Stre    m
	    Test
	public void streamSuccessTest1() throws IOException, InterruptedExc       ption {
		SimpleStreamBO bo = new Simp       eStreamBO();

		PinpointServerSocket ss = createServerSocket(new TestSeverMessageListener(), new        erverListener(bo));
		ss.       ind("localhost", 10234);

		PinpointSocketFactory pinpointSocket       a          tory = createSocketFactory();
		try {
			PinpointSocket socket = pinp          intSocketFactory.connect("127.0.0.1", 10234);

			RecordedStreamChannelMessageListener client          istener = new RecordedStreamChannelMessageListener(4);

			ClientStreamChannelContext clientCo          text = socket          createStreamChannel(new byte[0]              clientListe                   er);

			i          t sendCount = 4;

			for (int i = 0; i < sendCount; i++) {
				sendRan          omBytes(bo);
			}

			Thread.sleep(          00);

			       ssert.a          sertEquals(sendCount, clie          tList             ner.getReceivedMessage(    .si    e());

			clientContext.getStreamChannel().close();
			socket.close();
		        finally {
			pinpointSocketFactory.re       ease();
			ss.close();
		}
	}

	// Client to Server Stream
	@Test
	public void streamSuccessTest2       ) throws IOException, Int       rruptedException {
		SimpleStreamBO bo = new SimpleStreamBO();

       	          inpointServerSocket ss = createServerSocket(new TestSeverMessageListe          er(), new ServerListener(bo));
		ss.bind("localhost", 10234);

		PinpointSocketFactory pinpo          ntSocketFactory = createSocketFactory();
		try {
			PinpointSocket socket = pinpointSocketFact          ry.connect("127.0.0.1", 10234);

			RecordedStreamChannelMessageListener clientListener = new          RecordedStreamChannelMessageListener(4);
			ClientStreamChannelContext clientContext = socket.cr                   ateStre          mChannel(new byte[0], clientLis             ener);

			R                   cordedStre          mChannelMessageListener clientListener2 = new RecordedStreamChannelMe          sageListener(4);
			ClientStreamChannelContext clientContext2 = socket.          reateStreamChannel(new byte[0], clie          tListener2);

			int sendCount = 4;
			for (             nt i = 0; i                     sendCount           i++) {
				sendRandomBytes(bo);
			}

			Thread.sleep(100);

			Asse          t.assertEquals(sendCount, clientListener.getReceivedMessage().s                   ze());
			Assert.assertEquals(s                   ndCo       nt, cli          ntListener2.getReceivedMes          age()             si    e());

			clientContext.getStreamChannel().close();

			Thread.sleep(100)
			
			sendCount = 4;
			for (int i = 0; i < sendCount; i++) {
				sendRandomB       tes(bo);
			}

			Thread.       leep(100);

			Assert.assertEquals(sen       Count, clientListener.getReceivedMessage().size());
			Assert.assertEquals(8, clientListener2.getReceivedMe       s          ge().size());

			
			clientContext2.getStreamChannel().close();
			
          		socket.clos          ();
		} finally {
			pinpointSocketFactory.release();
			ss.close();

	}

	@Test
	public void streamSuccess          est3() throws IOException, InterruptedE          ception {
		PinpointServerSocket ss = createServerSocket(new TestSeverMessageListener(), null          ;
		ss.bind("localhost", 10234);

		SimpleStreamBO bo = new SimpleStreamBO();

		PinpointSocket          actory pinpoi          tSocketFactory = createSocketFa             tory(new Tes                   Listener()           new ServerListener(bo));

		try {
			PinpointSocket socket = pinpoint          ocketFactory.connect("127.0.0.1", 1          234);

		       Thread.          leep(100);

			List<Channe          Conte                t> contextList = ss.getDuplexCommunication    hannelContext();
			Assert.assertEquals(1, contextList.size());

			Chan       elContext context = contextList.get(0);

			RecordedStreamChannelMessageListene        clientListener = new Rec       rdedStreamChannelMessageListener(4);

			ClientStreamChannelCont       x           clientContext = context.createStreamChannel(new byte[0], clientListe          er);

			int sendCount = 4;

			for (int i = 0; i < sendCount; i++) {
				sendRandomBytes(bo)
			}

			Thread.sleep(100);

			Assert.assertEquals(sendCount, clientListener.getReceivedMess          ge().size());
			clientContext.getStreamChannel(          .close();       			sock          t.close();
		} finally {
	          	pinp             in    SocketFactory.release();
			ss.close();
		}
	}
	
	@Test(expected = Pinpo       ntSocketException.class)
	public void        treamClosedTest1() throws IOException, InterruptedException {
		PinpointServerSocket ss = createS       rverSocket(new TestSeverM       ssageListener(), null);
		ss.bind("localhost", 10234);

		Pinpoin       SocketFactory pinpointSoc       e          Factory = createSocketFactory();
		try {
			PinpointSo          ket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

			RecordedStreamChannelMess          geListener clientListener = new RecordedStreamChannelMessageListener(4);

			ClientStreamChan          elContext cli          ntContext = socket.createStreamChannel(new byte[0],                   clientListener);

			Thread.sl          ep(100);

			          lientContext.getStreamChannel().close();
			socket.c       ose();
          	} finally {
		             pinpoin                   SocketFactory.release()
			s                .close();
		}
	}

	@Test
	p          blic void streamClosedTest2() t    rows IOException, InterruptedException {
		Si    pleStreamBO bo = new SimpleStreamBO();

		PinpointServerSocket ss = crea       eServerSocket(new TestSeverMessageListener(), new ServerListener(bo));
		ss.bin       ("localhost", 10234);

		       inpointSocketFactory pinpointSocketFac       ory = createSocketFactory();

		PinpointSocket socket = null;
		try {
			socket = pinpointSocketFactory.con       ect("127.0.0.1", 10234);

			RecordedStreamChannelMessageListener clie       t                   istener            new RecordedStreamChannelMessageListener(4);

			ClientStreamChannelCo          text clientContext = socket.createStre          mChannel(new byte[0], clientListener);
          		Thread.sleep(100);

			Assert.assertEquals(1, bo.getStreamChannelContextSize());
			
			cli          ntContext.getStreamChannel().close();
			Thread.sleep(100);

			Assert.assertEquals(0, bo.getSt                   eamChannelContextSize());

		} finally {
			                   f (socket != null) {                   				socket          close();
			}
          			pinpointSocketFactory.release();       			ss.c          ose();

	}
	
	// ServerSocket to           lient                Stream
	
	
	// ServerStreamChannel first close.
	@Test(expected = PinpointSocketExcepti          n.class)
	public void streamClosedTest3() throws IOException, Inte       ruptedException {
		PinpointServerSocket ss = createServerSo       ket(new TestSeverMessageListene          (), null);
		ss.bind("localhost", 10234);

		Sim             leStreamBO bo = new SimpleStreamBO();

		Pin          ointSocketFactory pinpointSocketFactory = createSocketFactory(new TestListener(),             new ServerListe        r(bo));

		PinpointSocket socket = pinpointSocketFa       tory.connect("127.0.0.1", 10234);
		try {
			
			Thread.sleep(100);

	       	List<ChannelContext> con        xtList = ss.getDuplexCommunicationChannelContext();
			Assert.assertEquals(1, contextList.size());

			ChannelContext context = contextList.get(0);

			R       cordedStreamChannelMessageListener clientListener = new RecordedStream       hannelMessageListener(4);

			ClientStreamChannelConte       t clientContext = context.createStreamChannel(new byte[0], clientListener);

			
			StreamCha       nelContext aaa = socket.f        dStreamChannel(2);
			
			aaa.getStreamChannel().close()
			sendRandomBytes(bo);

			Thread.sleep(10       );


			clientContext.getStreamChannel().close();
       	} finally {
			socket.        ose();
			pinpointSocketFactory.release();
			ss.close();
		}
	}
	
       	private PinpointServerSocket       createServerSocket(ServerMessageListen          r sever             essa       eListener,
			ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
		PinpointServerSo          ket serverSocket = new PinpointServerSocket();

	          if              seve       MessageListener != null) {
			serverSocket.setMessageListener(severMessageListener);
		}

		if (server          treamChannelMessageListener != null) {
			serverSock             t.setServerStreamChan       elMessageListener(serverStreamChannelMessageListener);
		}

		return server       ocket;
	}

	private P          npointSocketFactory createSocketFactory() {
		PinpointSocketFactory pinpointSocket             actory = new PinpointSocketFactory();
		return pinpointSocketFactory;
	}

          private PinpointSocketFactory createSo             ketFactory(MessageListener messageListener, ServerStreamChannelMessageListene           serverStreamChannelMessageListener) {
		             inpointSocketFactory pinpoin          SocketFactory = new PinpointSocketFactory();
		pinpointSocketFactory.             etMessageListener(messageListener)
		pinpointSocketFactory.s          tServerStreamChannelMessageListener(ser          erStreamChannelMessageListener);

		return pinpointSocketFactory;
	}

	class TestListener extends SimpleLoggingMessageListener {

	}

	private void sendRandomBytes(SimpleStreamBO bo) {
		byte[] openBytes = TestByteUtils.createRandomByte(30);
		bo.sendResponse(openBytes);
	}

	class ServerListener implements ServerStreamChannelMessageListener {

		private final SimpleStreamBO bo;

		public ServerListener(SimpleStreamBO bo) {
			this.bo = bo;
		}

		@Override
		public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
			bo.addServerStreamChannelContext(streamChannelContext);
			return 0;
		}

		@Override
		public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
			bo.removeServerStreamChannelContext(streamChannelContext);
		}

	}

	class SimpleStreamBO {

		private final List<ServerStreamChannelContext> serverStreamChannelContextList;

		public SimpleStreamBO() {
			serverStreamChannelContextList = new CopyOnWriteArrayList<ServerStreamChannelContext>();
		}

		public void addServerStreamChannelContext(ServerStreamChannelContext context) {
			serverStreamChannelContextList.add(context);
		}

		public void removeServerStreamChannelContext(ServerStreamChannelContext context) {
			serverStreamChannelContextList.remove(context);
		}

		void sendResponse(byte[] data) {

			for (ServerStreamChannelContext context : serverStreamChannelContextList) {
				context.getStreamChannel().sendData(data);
			}
		}

		int getStreamChannelContextSize() {
			return serverStreamChannelContextList.size();
		}
	}

}
