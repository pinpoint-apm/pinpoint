package com.nhn.pinpoint.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author koo.taejin <kr14910>
 */
public final class NetUtils {

	public static final String LOOPBACK_ADDRESS_V4 = "127.0.0.1";
	
	private NetUtils() {
	}

	public static List<InetSocketAddress> toInetSocketAddressLIst(List<String> addressList) {
		List<InetSocketAddress> inetSocketAddressList = new ArrayList<InetSocketAddress>();

		for (String address : addressList) {
			InetSocketAddress inetSocketAddress = toInetSocketAddress(address);
			if (inetSocketAddress != null) {
				inetSocketAddressList.add(inetSocketAddress);
			}
		}

		return inetSocketAddressList;
	}

	public static InetSocketAddress toInetSocketAddress(String address) {
		try {
			URI uri = new URI("pinpoint://" + address);

			return new InetSocketAddress(uri.getHost(), uri.getPort());
		} catch (URISyntaxException e) {
		}

		return null;
	}

	public static String getLocalV4Ip() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String localIp = localHost.getHostAddress();
			if (validationIpV4FormatAddress(localIp)) {
				return localIp;
			}
		} catch (UnknownHostException e) {
		}
		return LOOPBACK_ADDRESS_V4;
	}
	
	/**
	 * 가지고 있는 외부에서 접근할수 있는 ip를 모두 반환합니다. 
	 * 만약 로컬 ip가 획득하지 못할 경우 Empty List를 반환합니다. 
	 */
	public static List<String> getLocalV4IpList() {
		List<String> result = new ArrayList<String>();
		
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
		}

		if (interfaces == null) {
			return Collections.EMPTY_LIST;
		}
		
		while (interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();
			if (isSkipIp(current)) {
				continue;
			}

			Enumeration<InetAddress> addresses = current.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				if (address.isLoopbackAddress() || !(address instanceof Inet4Address)) {
					continue;
				}
				
				if (validationIpV4FormatAddress(address.getHostAddress())) {
					result.add(address.getHostAddress());
				}
			}
		}

		return result;
	}
	
	private static boolean isSkipIp(NetworkInterface networkInterface) {
		try {
			if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
				return true;
			}
			return false;
		} catch (Exception e) {
		}
		return true;
	}
	

	public static boolean validationIpPortV4FormatAddress(String address) {
		try {
			int splitIndex = address.indexOf(':');

			if (splitIndex == -1 || splitIndex + 1 >= address.length()) {
				return false;
			}

			String ip = address.substring(0, splitIndex);
			
			if (!validationIpV4FormatAddress(ip)) {
				return false;
			}

			String port = address.substring(splitIndex + 1, address.length());
			if (Integer.parseInt(port) > 65535) {
				return false;
			}

			return true;
		} catch (Exception e) {
		}

		return false;
	}
	
	public static boolean validationIpV4FormatAddress(String address) {
		try {
			String[] eachDotAddress = address.split("\\.");
			if (eachDotAddress.length != 4) {
				return false;
			}

			for (String eachAddress : eachDotAddress) {
				if (Integer.parseInt(eachAddress) > 255) {
					return false;
				}
			}
			return true;
		} catch (NumberFormatException e) {
		}

		return false;
	}

}
