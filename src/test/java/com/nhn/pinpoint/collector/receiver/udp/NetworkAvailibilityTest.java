package com.nhn.pinpoint.collector.receiver.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

/**
 * 
 * @author netspider
 * 
 */
public class NetworkAvailibilityTest {

	@Test
	public void udp() {
		new Thread(new Server()).start();
		runClient();
		System.out.println("END");
	}

	private static class Server implements Runnable {
		public void run() {
			try {
				DatagramSocket s = new DatagramSocket(8888);

				byte[] receiveData = new byte[100];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				s.receive(receivePacket);
				System.out.println("SERVER RECEIVED=" + new String(receivePacket.getData()));

				String pong = "PONG";
				byte[] pongBytes = pong.getBytes();
				DatagramPacket pongPacket = new DatagramPacket(pongBytes, pongBytes.length, receivePacket.getSocketAddress());
				s.send(pongPacket);

				System.out.println("SERVER SEND=" + pong);

				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void runClient() {
		try {
			DatagramSocket s = new DatagramSocket();
			String data = "PING";
			byte[] dataBytes = data.getBytes();
			DatagramPacket datagramPacket = new DatagramPacket(dataBytes, dataBytes.length, InetAddress.getByName("127.0.0.1"), 8888);
			s.send(datagramPacket);
			System.out.println("CLIENT SEND=" + data);

			byte[] receiveData = new byte[100];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			s.receive(receivePacket);
			System.out.println("CLIENT RECEIVED=" + new String(receivePacket.getData()));

			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
