package com.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import com.profiler.config.TomcatProfilerConfig;
import com.profiler.dto.JVMInfoThriftDTO;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestThriftDTO;

/**
 * 
 * @author netspider
 * 
 */
public class DataSender extends Thread {

	private final Logger logger = Logger.getLogger(DataSender.class.getName());

	private final LinkedBlockingQueue<TBase<?, ?>> addedQueue = new LinkedBlockingQueue<TBase<?, ?>>(4096);

	private final InetSocketAddress requestDataAddr = new InetSocketAddress(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.REQUEST_DATA_LISTEN_PORT);
	private final InetSocketAddress requestTransactionDataAddr = new InetSocketAddress(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.REQUEST_TRANSACTION_DATA_LISTEN_PORT);
	private final InetSocketAddress jvmDataAddr = new InetSocketAddress(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.JVM_DATA_LISTEN_PORT);

	private static class SingletonHolder {
		public static final DataSender INSTANCE = new DataSender();
	}

	public static DataSender getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private DataSender() {
		setName("HIPPO-DataSender");
		setDaemon(true);
		start();
	}

	public boolean addDataToSend(TBase<?, ?> data) {
		// TODO: addedQueue가 full일 때 IllegalStateException처리.
		return addedQueue.add(data);
	}

	// TODO: send timeout추
	// TODO: addedqueue에서 bulk로 drain
	// TODO: sender thread가 한 개로 충분한가.
	public void run() {
		while (true) {
			DatagramSocket udpSocket = null;
			try {
				TBase<?, ?> dto = addedQueue.take();
                // TODO TSerializer대신에 HeaderTBaseSerializer로 하고 Header를 생성하여 같이 넘기면 되며
                // 받는 쪽에서. HeaderTBaseDeSerialize로 받으면 됨.
                // 단 header의 type 정보를 수동으로 넣어야 되는지가 불편함이 있음.
				TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
				byte[] sendData = serializer.serialize(dto);

				// TODO: 포트 하나로 통일 시켜야 함. 일단 임시로 이렇게..
				InetSocketAddress address = null;
				if (dto instanceof RequestDataListThriftDTO) {
					address = requestDataAddr;
				} else if (dto instanceof RequestThriftDTO) {
					address = requestTransactionDataAddr;
				} else if (dto instanceof JVMInfoThriftDTO) {
					address = jvmDataAddr;
				}

				if (address == null) {
					throw new IllegalArgumentException("Can't resolve receiver address.");
				}

				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address);

				udpSocket = new DatagramSocket();
				udpSocket.send(packet);

				//TODO: for test 로그레벨 바꾸기.
				logger.info(String.format("Data sent. %s", dto));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (udpSocket != null) {
					udpSocket.close();
				}
			}
		}
	}
}
