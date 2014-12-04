package com.nhn.pinpoint.testweb.configuration;

/**
 * 
 * @author netspider
 * 
 */
public abstract class DemoURLHolder {

	public abstract String getBackendWebURL();

	public abstract String getBackendApiURL();

	public static DemoURLHolder getHolder() {
		try {
			String hostname = java.net.InetAddress.getLocalHost().getHostName();

			if (hostname == null) {
				return new DemoURLHolderLocal();
			}

			if (hostname.endsWith("nhnsystem.com")) {
				return new DemoURLHolderDev();
			} else {
				return new DemoURLHolderLocal();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DemoURLHolderLocal();
		}
	}
}