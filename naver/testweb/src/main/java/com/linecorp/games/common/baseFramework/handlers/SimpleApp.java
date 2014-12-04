package com.linecorp.games.common.baseFramework.handlers;

public class SimpleApp {
	public static void main(String[] args) throws Exception {
		HttpCustomServerHandler handler = new HttpCustomServerHandler();
		System.out.println(handler);
		handler.messageReceived(null,  null);
        System.out.println("Hello PinPoint!---------");
        Thread.sleep(1000L);
		System.out.println("Hello PinPoint!=============");
	}
}
