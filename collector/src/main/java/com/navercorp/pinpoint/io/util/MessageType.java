package com.navercorp.pinpoint.io.util;


import com.navercorp.pinpoint.common.util.apache.IntHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MessageType {

    public static final MessageType EMPTY = new MessageType(-1);

    public static final MessageType SPAN = new MessageType(40);

    public static final MessageType AGENT_INFO = new MessageType(50);
    public static final MessageType AGENT_STAT = new MessageType(55);
    public static final MessageType AGENT_STAT_BATCH = new MessageType(56);
    public static final MessageType AGENT_URI_STAT = new MessageType(57);

    public static final MessageType PING = new MessageType(60);
    public static final MessageType PING_CLOSE = new MessageType(62);

    public static final MessageType SPANCHUNK = new MessageType(70);

    public static final MessageType SQLMETADATA = new MessageType(300);
    public static final MessageType SQLUIDMETADATA = new MessageType(301);
    public static final MessageType APIMETADATA = new MessageType(310);

    public static final MessageType STRINGMETADATA = new MessageType(330);
    public static final MessageType EXCEPTIONMETADATA = new MessageType(340);

    private static final IntHashMap<MessageType> MAP = buildMap();

    private static IntHashMap<MessageType> buildMap() {
        IntHashMap<MessageType> map = new IntHashMap<>();
        try {
            for (Field field : MessageType.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getType() == MessageType.class) {
                        MessageType messageType = (MessageType) field.get(MessageType.class);
                        MessageType exist = map.put(messageType.getCode(), messageType);
                        if (exist != null) {
                            throw new IllegalArgumentException("Duplicate code: " + messageType.getCode() + " for " + messageType + " and " + exist);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("MessageType initialization fail", e);
        }
        return map;
    }


    private final int code;

    private  MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


    public static MessageType getType(int code) {
        MessageType messageType = MAP.get(code);
        if (messageType != null) {
            return messageType;
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

}