package com.navercorp.pinpoint.io.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTypeTest {

    @Test
    void getType() {
        MessageType type = MessageType.getType(MessageType.SPAN.getCode());
        assertEquals(MessageType.SPAN, type);

        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageType.getType((short)-2));
    }
}