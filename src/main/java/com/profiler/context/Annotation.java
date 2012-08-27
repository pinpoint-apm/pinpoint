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

		@Override
		public String toString() {
			return "@RPCNAME={service=" + service + ", rpc=" + rpc + "}";
		}
	}

	public static class ClientAddr implements Annotation {
		private final String address;

		public ClientAddr(String address) {
			this.address = address;
		}

		@Override
		public String toString() {
			return "@CLIENT_ADDR=" + address;
		}
	}

	public static class ServerAddr implements Annotation {
		private final String address;

		public ServerAddr(String address) {
			this.address = address;
		}

		@Override
		public String toString() {
			return "@SERVER_ADDR=" + address;
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
