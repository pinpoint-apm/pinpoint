package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
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
            byte hasError = errorCode == 0 ? (byte) 0 : (byte) 1;
            buffer.putByte(hasError);
            buffer.putInt(elapsed);
            buffer.putPrefixedString(agentId);
            buffer.putSVInt(errorCode);
            return buffer.getBuffer();
        }

        public static Index decode(byte[] bytes, int offset, int length) {
            Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
            buffer.readByte();// hasError byte
            int elapsed = buffer.readInt();
            String agentId = buffer.readPrefixedString();
            int errorCode = buffer.readSVInt();
            return new Index(agentId, elapsed, errorCode);
        }
    }

    public record Meta(TransactionId transactionId, long startTime, String remoteAddr, String endpoint, String agentName) {
        public Meta(TransactionId transactionId, long startTime, String remoteAddr, String endpoint, String agentName) {
            this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
            this.startTime = startTime;
            this.remoteAddr = remoteAddr;
            this.endpoint = endpoint;
            this.agentName = agentName;
        }

        public static byte[] encode(TransactionId transactionId, long startTime, String remoteAddr, String endpoint, String agentName) {
            final Buffer buffer = new AutomaticBuffer(128);
            buffer.putByte((byte) 1); // version
            TransactionIdParser.writeTransactionIdV1(buffer, transactionId);

            buffer.putLong(startTime);
            buffer.putPrefixedString(remoteAddr);
            buffer.putPrefixedString(endpoint);
            buffer.putPrefixedString(agentName);
            return buffer.getBuffer();
        }

        public static Meta decode(byte[] bytes, int offset, int length) {
            Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
            buffer.readByte(); // version
            TransactionId transactionId = TransactionIdParser.readTransactionIdV1(buffer);

            long startTime = buffer.readLong();
            String remoteAddr = buffer.readPrefixedString();
            String endpoint = buffer.readPrefixedString();
            String agentName = buffer.readPrefixedString();
            return new Meta(transactionId, startTime, remoteAddr, endpoint, agentName);
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

