package com.profiler.context;

public interface Annotation {

	public static class ClientSend implements Annotation {
		@Override
		public String toString() {
			return "@CLIENT_SEND";
		}
	}

	public static class ClientRecv implements Annotation {
		@Override
		public String toString() {
			return "@CLIENT_RECV";
		}
	}

	public static class ServerSend implements Annotation {
		@Override
		public String toString() {
			return "@SERVER_SEND";
		}
	}

	public static class ServerRecv implements Annotation {
		@Override
		public String toString() {
			return "@SERVER_RECV";
		}
	}

	public static class Message implements Annotation {
		private String message;

		public Message(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}

		@Override
		public String toString() {
			return "@MSG=" + message;
		}
	}

	public static class RpcName implements Annotation {
		private final String service;
		private final String rpc;

		public RpcName(String service, String rpc) {
			this.service = service;
			this.rpc = rpc;
		}

		public String getService() {
			return service;
		}

		public String getRpc() {
			return rpc;
		}

		@Override
		public String toString() {
			return "@RPCNAME={service=" + service + ", rpc=" + rpc + "}";
		}
	}

	public static class ClientAddr implements Annotation {
		private final String ip;
		private final int port;

		public ClientAddr(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}

		@Override
		public String toString() {
			return "@CLIENT_ADDR={" + ip + ":" + port + "}";
		}
	}

	public static class ServerAddr implements Annotation {
		private final String ip;
		private final int port;

		public ServerAddr(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}

		@Override
		public String toString() {
			return "@SERVER_ADDR={" + ip + ":" + port + "}";
		}
	}

	public static class BinaryAnnotation implements Annotation {
		private final String key;
		private final String type;
		private final byte[] value;

		public BinaryAnnotation(String key, Object value) {
			this.key = key;
			this.type = value.getClass().getName();
			// TODO: convert to byte
			this.value = null;
		}
	}
}
