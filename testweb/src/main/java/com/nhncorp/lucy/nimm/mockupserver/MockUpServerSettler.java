package com.nhncorp.lucy.nimm.mockupserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.nhncorp.lucy.nimm.connector.NimmRunTimeException;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.nimm.connector.config.ConfigManager;
import com.nhncorp.lucy.nimm.connector.config.NimmConnector20Config;
import com.nhncorp.lucy.nimm.connector.config.NimmDivisionInfo;
import com.nhncorp.lucy.nimm.connector.config.ServerInformation;

public class MockUpServerSettler {

	private Logger log = Logger.getLogger(getClass().getName());

	private final Map<ServerInformation, NimmMockupServer> serverMap;

	private final MockupServerFactory serverFactory;

	public MockUpServerSettler(String resourceName) {

		NimmConnector20Config configuration = null;
		try {
			URL configURL = MockUpServerSettler.class.getResource(resourceName);
			ConfigManager configManager = new ConfigManager();
			configuration = configManager.getConfig(configURL, true);
		} catch (Exception e) {
			throw new NimmRunTimeException("Configuration couldn't be loaded", e);
		}

		this.serverFactory = new MockupServerFactory(1);

		NimmDivisionInfo mgmtInfo = configuration.getNimmMgmtInfo();
		NimmDivisionInfo svcInfo = configuration.getNimmServiceInfo();
		this.serverMap = new HashMap<ServerInformation, NimmMockupServer>();

		verifyThenAddServerInfo(NimmAddress.Species.Management, mgmtInfo);
		verifyThenAddServerInfo(NimmAddress.Species.Service, svcInfo);


	}

	private void verifyThenAddServerInfo(NimmAddress.Species species, NimmDivisionInfo divisionInfo) {

		if(divisionInfo == null) {
			throw new RuntimeException("There isn't" + species.toString() + "Info");
		}

		if (validateServerInformation(divisionInfo.getNimmServerAddress())) {
			throw new RuntimeException(species.toString() + " Server Infos are wrong.");
		}

		for(Map.Entry<String, List<ServerInformation>> catalogEntry : divisionInfo.getNimmServerAddress().entrySet()) {
			for(ServerInformation serverInfo : catalogEntry.getValue()) {

				int port = serverInfo.getInetSocketAddress().getPort();
				NimmMockupServer mockupServer = serverFactory.createMockUpServer(species, port);
				this.serverMap.put(serverInfo, mockupServer);
			}
		}

	}

	public void setUp() {


		for(Map.Entry<ServerInformation, NimmMockupServer> entry : serverMap.entrySet()) {
			try {
				NimmMockupServer mockupServer = entry.getValue();
				mockupServer.start();
			} catch (IOException e) {
				if (log.isLoggable(Level.WARNING)) {
					log.log(Level.WARNING, "Can't Activate MockUpServer " + entry.getKey(), e);
				}
				throw new RuntimeException("mockupServer start error", e);
			}
		}
	}

	public void setDown() {
		boolean error = false;
		for (Map.Entry<ServerInformation, NimmMockupServer> entry : this.serverMap.entrySet()) {
			try {
				ServerInformation serverInfo = entry.getKey();
				NimmMockupServer mockupServer = entry.getValue();
				mockupServer.stop();
				if(log.isLoggable(Level.INFO)) {
					log.info(serverInfo + " has been stopped.");
				}
			} catch(Throwable th){
				log.log(Level.WARNING, "MockUPServer setDown error", th);
			}
		}
		if(error) {
			throw new RuntimeException("check log down error");
		}
	}

	public void setGracefulDown() {
		boolean error = false;
		for (Map.Entry<ServerInformation, NimmMockupServer> entry : this.serverMap.entrySet()) {
			try {
				ServerInformation serverInfo = entry.getKey();
				NimmMockupServer mockupServer = entry.getValue();
				mockupServer.gracefulStop();
				if(log.isLoggable(Level.INFO)) {
					log.info(serverInfo + " has been graceful stopped.");
				}
			} catch(Throwable th){
				log.log(Level.WARNING, "MockUPServer setGracefulDown error", th);
				error = true;
			}
		}
		if(error) {
			throw new RuntimeException("check log gracefuldown error");
		}
	}

	private boolean validateServerInformation(Map<String, List<ServerInformation>> serverCatalog) {


		if(serverCatalog == null || serverCatalog.isEmpty())
			return false;

		Set<Integer> portSet = new HashSet<Integer>();
		InetAddress localHost = null;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// don't need to handle exception
		}
		byte[] loopbackIP = new byte[4];
		loopbackIP[0] = 127;
		loopbackIP[1] = loopbackIP[2] = 0;
		loopbackIP[3] = 1;


		for(Map.Entry<String, List<ServerInformation>> catalogEntry : serverCatalog.entrySet()) {

			for(ServerInformation serverInfo : catalogEntry.getValue()) {
				InetSocketAddress inetAddress = serverInfo.getInetSocketAddress();
				if (!portSet.add(inetAddress.getPort()))
					return false;
				byte[] serverIP = inetAddress.getAddress().getAddress();
				// TODO array를 잘못비교하고 있는것으로 보임.  equals수정해야 되는지 검토
				if (localHost != null && (localHost.getAddress().equals(serverIP)))
					continue;
				else if (!serverIP.equals(loopbackIP))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Add pre-registered NimmConnector Address
	 * 
	 * @param domainId Domain Id of pre-registered NimmConnector
	 * @param serverId Server Id of pre-registered NimmConnector
	 */
	public void addPreRegisteredConnector(int domainId, int serverId) {
		for (Map.Entry<ServerInformation, NimmMockupServer> serverEntry : serverMap.entrySet()) {
			NimmMockupServer server = serverEntry.getValue();
			server.addDummyClientManually(domainId, serverId);
		}
	}

	/**
	 * Remove pre-registered NimmConnector Address
	 *
	 * @param domainId Domain Id of pre-registered NimmConnector
	 * @param serverId Server Id of pre-registered NimmConnector
	 */
	public void removePreRegisteredConnector(int domainId, int serverId) {
		for (Map.Entry<ServerInformation, NimmMockupServer> serverEntry : serverMap.entrySet()) {
			NimmMockupServer server = serverEntry.getValue();
			server.removeDummyClientManually(domainId, serverId);
		}
	}

}
