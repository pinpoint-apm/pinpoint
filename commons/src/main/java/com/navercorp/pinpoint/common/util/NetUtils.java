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

package com.navercorp.pinpoint.common.util;

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
 * @author koo.taejin
 */
public final class NetUtils {

    public static final String LOOPBACK_ADDRESS_V4 = "127.0.0.1       ;
	
	private NetU        ls() {
	}

	public static List<InetSocketAddress> toInetSocketAddressLIst(List<String>        ddressList) {
		List<InetSocketAddress> inetSocketAddressList = new ArrayList<In       tSocketAddress>();

		for (Strin           address : addressList) {
			InetSocketAddress inetSocketAddr          ss = toInetSocketAddress(a             dress);
			if (inetSocketAddress != n                      ll) {
				inetSocket        dressList.add(inetSocketAddress);
			}
		}

		return inetSocketAddr       s          List;
	}

	public static InetSocketAdd          ess toInetSocketAddress(String address) {
		try {
			       RI uri = new URI("pinpoint://" + address);

			return             new Ine        ocketAddress(uri.getHost(), uri.get       o          t());
		} catch (URISyntaxException ignore) {                      // skip
		}

		return null
	}

	public static String getLocalV4             p() {
	                try {
			InetAddress localHost = InetAddress.getLoc             lHost();
			String lo              lIp = localHost.getHostAddress();
			if (validationIpV4FormatAddress(localIp)) {
				re    urn localIp;
			}
		} catch (UnknownHostException ignore) {
            // ski
    	}
		return LOOPBACK_ADDRESS_V4;
	}
	
	/**
	         Returns a list of ip addreses on this mac             ine that is accessible from a remote sour       e
	 * If no network interfaces can be found on t       is machine, returns an empty List.
	 */
	public st             tic List<String> get          ocalV4IpList() {
		List<                   tring> result = new ArrayList          String>();
		
		Enumeration<NetworkInterface>           nterfaces = null;
             	                   ry {
			interfaces = NetworkInterface.getNetworkInterfa          es();
		} catch (SocketExceptio              ignore) {
            // skip
		}

		             f (interfaces == null) {
			return Collections.EMPTY_LIST;
		}

		while (interfaces.hasMoreElements())                {
			NetworkInterface curre                                           t = interfaces.nextElement();
			if (isSkipIp(current)) {
				con       i          ue;
			}

			Enumeration<InetAddress> addresses = current.getInetAddresses();
			while (ad             ress                   s.h       sMoreElements()) {
				InetAddress address =             addres          es.nextElement();
				if (address.isLoopbackAddress() || !(address in       t          nceof Inet4Address)) {
					conti          ue;
				}
				
				if (validationIpV4FormatAddress(addr             ss.ge                   HostAddress())) {
					result.add(ad                   ress.getHostAddress());

			}                   		}

		return result;
	}
	
	private static boolean isSkip          p(NetworkInterface networkInter             ace)
		       ry {
			if (!networkInterface.isUp() || net             orkInter          ace.isLoopback() || networkInterface.isVirtual()) {
				return t       u          ;
			}
			return false;
		} catch (Except          on ignore) {
            //             skip
                   	}
		return true;
	}
	

	public st             tic boolean validationIpPortV4Form                tAd                                     ress(String address) {
		try {
			int splitIndex = ad             ress.ind    xOf(':');

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
		} catch (Exception ignore) {
            //skip
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
		} catch (NumberFormatException ignore) {
            // skip
		}

		return false;
	}

}
