package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;

/**
 * @author hyungil.jeong
 */
public class AgentStatCpuLoadBo {

	private final String agentId;
	private final long startTimestamp;
	private final long timestamp;
	private final double jvmCpuLoad;
	private final double systemCpuLoad;

	private AgentStatCpuLoadBo(Builder builder) {
		this.agentId = builder.agentId;
		this.startTimestamp = builder.startTimestamp;
		this.timestamp = builder.timestamp;
		this.jvmCpuLoad = builder.jvmCpuLoad;
		this.systemCpuLoad = builder.systemCpuLoad;
	}

	public String getAgentId() {
		return agentId;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public double getJvmCpuLoad() {
		return jvmCpuLoad;
	}

	public double getSystemCpuLoad() {
		return systemCpuLoad;
	}

	public byte[] writeValue() {
		final Buffer buffer = new AutomaticBuffer();
		buffer.putPrefixedString(this.agentId);
		buffer.put(this.startTimestamp);
		buffer.put(this.timestamp);
		buffer.put(this.jvmCpuLoad);
		buffer.put(this.systemCpuLoad);
		return buffer.getBuffer();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AgentStatCpuLoadBo{");
		sb.append("agentId='").append(this.agentId).append('\'');
		sb.append(", startTimestamp=").append(this.startTimestamp);
		sb.append(", timestamp=").append(this.timestamp);
		sb.append(", jvmCpuLoad=").append(this.jvmCpuLoad);
		sb.append(", systemCpuLoad=").append(this.systemCpuLoad);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
		result = prime * result + (int)(startTimestamp ^ (startTimestamp >>> 32));
		result = prime * result + (int)(timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentStatCpuLoadBo other = (AgentStatCpuLoadBo)obj;
		if (agentId == null) {
			if (other.agentId != null)
				return false;
		} else if (!agentId.equals(other.agentId))
			return false;
		if (startTimestamp != other.startTimestamp)
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	public static class Builder {
		private static final double UNSUPPORTED = -1.0D;
		private final String agentId;
		private final long startTimestamp;
		private final long timestamp;
		private double jvmCpuLoad = UNSUPPORTED;
		private double systemCpuLoad = UNSUPPORTED;

		public Builder(final byte[] value) {
			final Buffer buffer = new FixedBuffer(value);
			this.agentId = buffer.readPrefixedString();
			this.startTimestamp = buffer.readLong();
			this.timestamp = buffer.readLong();
			this.jvmCpuLoad = buffer.readDouble();
			this.systemCpuLoad = buffer.readDouble();
		}

		public Builder(String agentId, long startTimestamp, long timestamp) {
			this.agentId = agentId;
			this.startTimestamp = startTimestamp;
			this.timestamp = timestamp;
		}

		public Builder jvmCpuLoad(double jvmCpuLoad) {
			this.jvmCpuLoad = jvmCpuLoad;
			return this;
		}

		public Builder systemCpuLoad(double systemCpuLoad) {
			this.systemCpuLoad = systemCpuLoad;
			return this;
		}

		public AgentStatCpuLoadBo build() {
			return new AgentStatCpuLoadBo(this);
		}
	}
}
