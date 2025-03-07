package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.util.Bytes;

public class ApplicationUidRowKeyUtils {

    public static byte[] makeRowKey(ServiceUid serviceUid, String applicationName) {
        final byte[] serviceUidBytes = Bytes.toBytes(serviceUid.getUid());
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(2 + applicationNameBytes.length + 8);
        buffer.put2PrefixedString(applicationName);
        buffer.putBytes(serviceUidBytes);
        return buffer.getBuffer();
    }

    public static byte[] makeRowKey(String applicationName) {
        Buffer buffer = new AutomaticBuffer();
        buffer.put2PrefixedString(applicationName);
        return buffer.getBuffer();
    }

    public static int getServiceUidFromRowKey(byte[] rowKey) {
        short applicationNameLength = Bytes.toShort(rowKey, 0);
        return Bytes.toInt(rowKey, 2 + applicationNameLength);
    }

}
