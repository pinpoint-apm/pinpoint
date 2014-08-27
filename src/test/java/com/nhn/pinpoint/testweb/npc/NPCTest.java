package com.nhn.pinpoint.testweb.npc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

public class NPCTest {

	@Test
	public void connect() {
		try {
			InetSocketAddress serverAddress = new InetSocketAddress("0.0.0.0", 5000);
			NpcHessianConnector connector = new NpcHessianConnector(serverAddress, true);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("message", "hello pinpoint");

			InvocationFuture future = connector.invoke("welcome/com.nhncorp.lucy.bloc.welcome.EchoBO", "execute", params);

			future.await();

			// Object result = future.get();
			Object result = future.getReturnValue();

			System.out.println(result);
			Assert.assertNotNull(result);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
