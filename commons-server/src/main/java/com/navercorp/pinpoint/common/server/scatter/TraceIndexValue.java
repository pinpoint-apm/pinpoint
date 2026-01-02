package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.util.TransactionIdParser;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

public class TraceIndexValue {

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

    public record Meta(ServerTraceId transactionId, long startTime, String remoteAddr, String endpoint, String agentName) {
        public Meta(ServerTraceId transactionId, long startTime, String remoteAddr, String endpoint, String agentName) {
            this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
            this.startTime = startTime;
            this.remoteAddr = remoteAddr;
            this.endpoint = endpoint;
            this.agentName = agentName;
        }

        public static byte[] encode(ServerTraceId transactionId, long startTime, String remoteAddr, String endpoint, String agentName) {
            final Buffer buffer = new AutomaticBuffer(128);
            buffer.putLong(startTime);
            buffer.putByte((byte) 1); // version
            TransactionIdParser.writeTransactionIdV1(buffer, transactionId);
            buffer.putPrefixedString(remoteAddr);
            buffer.putPrefixedString(endpoint);
            buffer.putPrefixedString(agentName);
            return buffer.getBuffer();
        }

        public static Meta decode(byte[] bytes, int offset, int length) {
            Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
            long startTime = buffer.readLong();
            buffer.readByte(); // version
            ServerTraceId serverTraceId = PinpointServerTraceId.of(buffer);
            String remoteAddr = buffer.readPrefixedString();
            String endpoint = buffer.readPrefixedString();
            String agentName = buffer.readPrefixedString();
            return new Meta(serverTraceId, startTime, remoteAddr, endpoint, agentName);
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

