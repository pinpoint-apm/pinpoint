package com.navercorp.pinpoint.grpc.channelz;


import java.net.InetSocketAddress;
import java.util.Set;

public interface ChannelzRegistry {

    void addSocket(long logId, InetSocketAddress remoteAddress, InetSocketAddress localAddress);

    Long removeSocket(InetSocketAddress remoteAddress);

    Set<Long> getSocketLogId(AddressId address);

    void addServer(long logId, String serverName);

    Long getServerLogId(String serverName);


    class AddressId {
        private final String address;
        private final int port;

        public static AddressId newAddressId(String address, int port) {
            return new AddressId(address, port);
        }

        public static AddressId newAddressId(InetSocketAddress local, InetSocketAddress remote) {
            return new AddressId(remote.getHostString(), local.getPort());
        }

        private AddressId(String address, int localPort) {
            this.address = address;
            this.port = localPort;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AddressId addressId = (AddressId) o;

            if (port != addressId.port) return false;
            return address != null ? address.equals(addressId.address) : addressId.address == null;
        }

        @Override
        public int hashCode() {
            int result = port;
            result = 31 * result + (address != null ? address.hashCode() : 0);
            return result;
        }

    }

}
