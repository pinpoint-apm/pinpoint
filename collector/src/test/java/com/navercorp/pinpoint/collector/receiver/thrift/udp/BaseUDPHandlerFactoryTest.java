package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.PooledObject;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseUDPHandlerFactoryTest {

    @Mock
    private DispatchHandler<TBase<?, ?>, TBase<?, ?>> dispatchHandler;
    @Mock
    private TBaseFilter<SocketAddress > filter;
    @Mock
    private AddressFilter ignoreAddressFilter;
    @Mock
    private DatagramSocket socket;

    private HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory().createSerializer();

    @Test
    public void receive() throws Exception {
        byte[] bytes = serializer.serialize(new TSpan());

        PooledObject<DatagramPacket> pool = mock(PooledObject.class);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        packet.setSocketAddress(new InetSocketAddress(80));
        when(pool.getObject()).thenReturn(packet);
        when(filter.filter(any(), any(), any())).thenReturn(true);
        when(ignoreAddressFilter.accept(any())).thenReturn(true);


        PacketHandlerFactory<DatagramPacket> factory = new BaseUDPHandlerFactory<>(dispatchHandler, filter, ignoreAddressFilter);
        PacketHandler<DatagramPacket> handler = factory.createPacketHandler();

        handler.receive(socket, pool);

        // check memory leak
        verify(pool).returnObject();
        verify(dispatchHandler).dispatchSendMessage(any());
    }
}