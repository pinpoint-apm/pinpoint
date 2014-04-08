package com.nhn.pinpointtest;

public class SimpleApp {
	public static void main(String[] args) throws Exception {
		HttpCustomServerHandler handler = new HttpCustomServerHandler();
		System.out.println(handler);
		handler.messageReceived(null,  null);
		System.out.println("Hello PinPoint!");
	}
}
