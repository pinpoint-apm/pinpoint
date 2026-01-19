package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.util.TransactionIdParser;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

public class TraceIndexValue {

    public static final byte TRACE_TYPE_PINPOINT = (byte) 1;
    public static final byte TRACE_TYPE_OTEL = (byte) 2;

    public record Index(String agentId, int elapsed, int errorCode) {
        public Index(String agentId, int elapsed, int errorCode) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.elapsed = elapsed;
            this.errorCode = errorCode;
        }

        public static byte[] encode(String agentId, int elapsed, int errorCode) {
            final Buffer buffer = new AutomaticBuffer(64);
            buffer.putInt(elapsed);
            buffer.putPrefixedString(agentId);
            buffer.putSVInt(errorCode);
            return buffer.getBuffer();
        }

        public static Index decode(byte[] bytes, int offset, int length) {
            Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
            int elapsed = buffer.readInt();
            String agentId = buffer.readPrefixedString();
            int errorCode = buffer.readSVInt();
            return new Index(agentId, elapsed, errorCode);
        }
    }

    public record Meta(ServerTraceId serverTraceId, long startTime, String remoteAddr, String endpoint,
                       String agentName) {
        public Meta(ServerTraceId serverTraceId, long startTime, String remoteAddr, String endpoint, String agentName) {
            this.serverTraceId = Objects.requireNonNull(serverTraceId, "serverTraceId");
            this.startTime = startTime;
            this.remoteAddr = remoteAddr;
            this.endpoint = endpoint;
            this.agentName = agentName;
        }

        public static byte[] encode(ServerTraceId serverTraceId, long startTime, String remoteAddr, String endpoint, String agentName) {
            final Buffer buffer = new AutomaticBuffer(128);
            buffer.putLong(startTime);
            encodeServerTraceId(buffer, serverTraceId);
            buffer.putPrefixedString(remoteAddr);
            buffer.putPrefixedString(endpoint);
            buffer.putPrefixedString(agentName);
            return buffer.getBuffer();
        }

        public static Meta decode(byte[] bytes, int offset, int length) {
            Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
            long startTime = buffer.readLong();
            ServerTraceId serverTraceId = decodeServerTraceId(buffer);
            String remoteAddr = buffer.readPrefixedString();
            String endpoint = buffer.readPrefixedString();
            String agentName = buffer.readPrefixedString();
            return new Meta(serverTraceId, startTime, remoteAddr, endpoint, agentName);
        }


        private static void encodeServerTraceId(Buffer buffer, ServerTraceId serverTraceId) {
            if (serverTraceId instanceof PinpointServerTraceId pinpointServerTraceId) {
                buffer.putByte(TRACE_TYPE_PINPOINT);
                TransactionIdParser.writeTransactionIdV1(buffer, pinpointServerTraceId);
            } else if (serverTraceId instanceof OtelServerTraceId otelServerTraceId) {
                buffer.putByte(TRACE_TYPE_OTEL);
                buffer.putBytes(otelServerTraceId.getId());
            } else {
                throw new IllegalArgumentException("Unknown ServerTraceId implementation: " + serverTraceId.getClass());
            }
        }

        private static ServerTraceId decodeServerTraceId(Buffer buffer) {
            byte type = buffer.readByte();
            if (type == TRACE_TYPE_PINPOINT) {
                return PinpointServerTraceId.of(buffer);
            } else if (type == TRACE_TYPE_OTEL) {
                return OtelServerTraceId.of(buffer);
            } else {
                throw new IllegalArgumentException("Unknown ServerTraceId type: " + type);
            }
        }
    }

    public record MetaRpc(String rpc) {
        public MetaRpc(String rpc) {
            this.rpc = Objects.requireNonNull(rpc, "rpc");
        }

        public static byte[] encode(String rpc) {
            return Bytes.toBytes(rpc);
        }

        public static MetaRpc decode(byte[] bytes, int offset, int length) {
            String rpc = Bytes.toString(bytes, offset, length);
            return new MetaRpc(rpc);
        }
    }
}

