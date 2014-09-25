package com.nhn.pinpoint.testweb.nimm.mockupserver;

import java.nio.ByteBuffer;

import java.util.logging.Logger;

//import org.junit.Assert;
import org.springframework.stereotype.Component;

import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.net.call.DefaultReturnValue;
import com.nhncorp.lucy.net.call.Reply;
import com.nhncorp.lucy.net.call.ReturnValue;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.net.invoker.Invoker;
import com.nhncorp.lucy.nimm.connector.NimmConnector;
import com.nhncorp.lucy.nimm.connector.NimmSocket;
import com.nhncorp.lucy.nimm.connector.bloc.NimmInvoker;
import com.nhncorp.lucy.nimm.connector.message.NimmMessage;
import com.nhncorp.lucy.nimm.connector.worker.NimmAbstractWorkerMock;
import com.nhncorp.lucy.npc.DefaultNpcMessage;
import com.nhncorp.lucy.npc.NpcMessage;
import com.nhncorp.lucy.npc.decoder.NpcHessianDecoder;
import com.nhncorp.lucy.npc.encoder.NpcHessianEncoder;

import external.org.apache.mina.filter.codec.ProtocolEncoderException;

/**
 * <pre>
 * original source : com.nhncorp.lucy.nimm.connector.bloc.NimmInvokerTest
 * </pre>
 * 
 * @author netspider
 * 
 */
@Component
public class NimmInvokerTest extends AbstractNimmTest {

	private Logger log = Logger.getLogger(getClass().getName());

	private Invoker invoker;

	private NimmSocket sendSocket;
	private NimmSocket receiveSocket;

	private static int SEND_SOCKET_DOMAIN_ID = 101;
	private static int SEND_SOCKET_SOCKET_ID = 13;

	private static int RECEIVE_SOCKET_DOMAIN_ID = 103;
	private static int RECEIVE_SOCKET_SOCKET_ID = 15;

	public NimmInvokerTest() {
		try {
			initialize("/NimmMockupServer.xml");

			sendSocket = NimmConnector.createNimmSocket(SEND_SOCKET_DOMAIN_ID, SEND_SOCKET_SOCKET_ID);
			receiveSocket = NimmConnector.createNimmSocket(RECEIVE_SOCKET_DOMAIN_ID, RECEIVE_SOCKET_SOCKET_ID);
			invoker = new NimmInvoker(receiveSocket.getAddress(), sendSocket);

			NimmInvoker nimmInvoker = (NimmInvoker) invoker;
			long currentTimeout = nimmInvoker.getTimeout();
			nimmInvoker.setTimeout(currentTimeout + 1000L);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void tearDown() {
		if (sendSocket != null) {
			sendSocket.dispose();
		}
		if (receiveSocket != null) {
			receiveSocket.dispose();
		}
		if (invoker != null) {
			invoker.dispose();
			// Assert.assertFalse(invoker.isValid());
			if (!invoker.isValid()) {
				throw new RuntimeException("invokoer is not valid.");
			}
		}
	}

	public void testInvoke() throws Exception {
		log.info("setWorker");

		receiveSocket.setWorker(new NimmAbstractWorkerMock() {
			@Override
			protected ByteBuffer responseMessage(NimmMessage request) throws Exception {
				log.info("message response");

				ByteBuffer npcHessianByteBuffer = request.getMessage();
				NpcMessage decodeCall = NpcHessianDecoder.decodeCall(npcHessianByteBuffer);
				Call call = (Call) decodeCall.getPayload();
				log.info("Call:" + call);

				DefaultReturnValue result = null;
				if (decodeCall.getNamespace() == null) {
					result = new DefaultReturnValue("response without namespace");
				} else {
					result = new DefaultReturnValue("response");
				}

				DefaultNpcMessage reply = new DefaultNpcMessage();
				reply.setNamespace(decodeCall.getNamespace());
				reply.setTransactionId(decodeCall.getTransactionId());
				reply.setPayloadCharset(decodeCall.getPayloadCharset());
				reply.setPayload(result);
				return NpcHessianEncoder.encodeReply(reply);
			}
		});

		InvocationFuture future = invoker.invoke("objectName", "methodName", "params");
		log.info("await");
		future.await();
		Reply reply = (Reply) future.getReturnValue();
		if (reply instanceof ReturnValue) {
			ReturnValue value = (ReturnValue) reply;
			String message = (String) value.get();

			if (!"response".equals(message)) {
				throw new RuntimeException("invalid response, " + message);
			}
			// Assert.assertEquals("response", message);
		} else {
			throw new RuntimeException("reply retrun type=" + reply);
			// Assert.fail("reply retrun type=" + reply);
		}

		// Without object name
		future = invoker.invoke(null, "methodName", "params");
		log.info("await");
		future.await();
		reply = (Reply) future.getReturnValue();
		if (reply instanceof ReturnValue) {
			ReturnValue value = (ReturnValue) reply;
			String message = (String) value.get();

			if (!"response without namespace".equals(message)) {
				throw new RuntimeException("invalid response, " + message);
			}
			// Assert.assertEquals("response without namespace", message);
		} else {
			throw new RuntimeException("reply retrun type=" + reply);
			// Assert.fail("reply retrun type=" + reply);
		}

		// Assert.assertTrue(invoker.isValid());
		if (!invoker.isValid()) {
			throw new RuntimeException("invokoer is not valid.");
		}
		
		invoker.dispose();
	}

	public void testConstructorWithInvalidParams() throws Exception {
		Invoker invalidInvoker = null;

		// First parameters
		try {
			invalidInvoker = new NimmInvoker(null, sendSocket);
		} catch (NullPointerException ex) {
			// TODO make the tested code have a better exception like
			// NimmRuntimeException
			String msg = ex.getMessage();

			if (msg.indexOf("nimmBlocAddress") == -1) {
				throw new RuntimeException("Invalid NullPointerException for nimmBlocAddress");
				// Assert.fail("Invalid NullPointerException for nimmBlocAddress");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Invaid Exception for nimmBlocAddress", ex);
			// Assert.fail("Invaid Exception for nimmBlocAddress - " +
			// ex.getMessage());
		}

		// Second parameters
		try {
			invalidInvoker = new NimmInvoker(receiveSocket.getAddress(), null);
		} catch (NullPointerException ex) {
			// TODO make the tested code have a better exception like
			// NimmRuntimeException
			String msg = ex.getMessage();
			if (msg.indexOf("nimmLocalSocket") == -1) {
				throw new RuntimeException("Invalid NullPointerException for nimmLocalSocket");
				// Assert.fail("Invalid NullPointerException for nimmLocalSocket");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Invaid Exception for nimmLocalSocket", ex);
			// Assert.fail("Invaid Exception for nimmLocalSocket - " +
			// ex.getMessage());
		}

		// Third parameters
		try {
			invalidInvoker = new NimmInvoker(receiveSocket.getAddress(), sendSocket, -1000L);
		} catch (IllegalArgumentException ex) {
			String msg = ex.getMessage();
			if (msg.indexOf("timeoutMillis") == -1) {
				throw new RuntimeException("Invalid IllegalArgumentException for timeoutMillis");
				// Assert.fail("Invalid IllegalArgumentException for timeoutMillis");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Invaid Exception for timeoutMillis - " + ex.getMessage());
			// Assert.fail("Invaid Exception for timeoutMillis - " +
			// ex.getMessage());
		}

		if (invalidInvoker != null) {
			invalidInvoker.dispose();
		}
	}

	public void testInvokeWithInvalidParams() throws Exception {
		// methodName
		try {
			invoker.invoke("objectName", null, "params");
		} catch (NullPointerException ex) {
			// TODO make the tested code have a better exception like
			// NimmRuntimeException
			String msg = ex.getMessage();
			if (msg.indexOf("methodName") == -1) {
				throw new RuntimeException("Invalid NullPointerException for methodName");
				// Assert.fail("Invalid NullPointerException for methodName");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Invaid Exception for methodName - " + ex.getMessage());
			// Assert.fail("Invaid Exception for methodName - " +
			// ex.getMessage());
		}

		// timeoutMillis
		try {
			NimmInvoker nimmInvoker = (NimmInvoker) invoker;
			nimmInvoker.invoke(-1000L, "objectName", "methodName", "params");
		} catch (IllegalArgumentException ex) {
			String msg = ex.getMessage();
			if (msg.indexOf("timeoutMillis") == -1) {
				throw new RuntimeException("Invalid IllegalArgumentException for timeoutMillis");
				// Assert.fail("Invalid IllegalArgumentException for timeoutMillis");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Invaid Exception for timeoutMillis - " + ex.getMessage());
			// Assert.fail("Invaid Exception for timeoutMillis - " +
			// ex.getMessage());
		}

		// setTimeoutMillis
		try {
			NimmInvoker nimmInvoker = (NimmInvoker) invoker;
			nimmInvoker.setTimeout(-1000L);
		} catch (IllegalArgumentException ex) {
			String msg = ex.getMessage();
			if (msg.indexOf("timeoutMillis") == -1) {
				throw new RuntimeException("Invalid IllegalArgumentException for timeoutMillis");
				// Assert.fail("Invalid IllegalArgumentException for timeoutMillis");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Invaid Exception for timeoutMillis - " + ex.getMessage());
			// Assert.fail("Invaid Exception for timeoutMillis - " +
			// ex.getMessage());
		}

		// params : java.lang.Class cannot be encoded.
		InvocationFuture future = invoker.invoke("objectName", "methodName", Integer.class);
		log.info("await");
		future.await();
		Exception ex = (Exception) future.getException();

		if (!(ex instanceof ProtocolEncoderException)) {
			throw new RuntimeException("!(ex instanceof ProtocolEncoderException)");
		}
		// Assert.assertTrue(ex instanceof ProtocolEncoderException);

		invoker.dispose();
	}
}
