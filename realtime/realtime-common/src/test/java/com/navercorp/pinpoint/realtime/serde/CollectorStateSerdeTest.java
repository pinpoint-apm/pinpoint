package com.navercorp.pinpoint.realtime.serde;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.vo.CollectorState;
import com.navercorp.pinpoint.realtime.vo.ProfilerDescription;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectorStateSerdeTest {

    @Test
    void serde() throws IOException {
        List<ProfilerDescription> profilers = List.of(
                new ProfilerDescription(new ClusterKey("app1", "agent1", 1)),
                new ProfilerDescription(new ClusterKey("app2", "agent2", 1))
        );
        CollectorState state = new CollectorState(profilers);

        CollectorStateSerde serde = new CollectorStateSerde();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        serde.serialize(state, outputStream);
        CollectorState deserializedState = serde.deserialize(new ByteArrayInputStream(outputStream.toByteArray()));

        assertEquals(state.getProfilers(), deserializedState.getProfilers());
    }

}