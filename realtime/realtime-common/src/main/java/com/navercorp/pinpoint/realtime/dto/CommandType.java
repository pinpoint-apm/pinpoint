package com.navercorp.pinpoint.realtime.dto;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;

public enum CommandType {
    // Using reflection would make code cleaner.
    // But it also makes it hard to handle exception, constructor and will show relatively low performance.

    RESULT((short) 320),

    TRANSFER((short) 700),
    TRANSFER_RESPONSE((short) 701),

    ECHO((short) 710),

    THREAD_DUMP((short) 720),
    THREAD_DUMP_RESPONSE((short) 721),

    ACTIVE_THREAD_COUNT((short) 730),
    ACTIVE_THREAD_COUNT_RESPONSE((short) 731),

    ACTIVE_THREAD_DUMP((short) 740),
    ACTIVE_THREAD_DUMP_RESPONSE((short) 741),

    ACTIVE_THREAD_LIGHT_DUMP((short) 750),
    ACTIVE_THREAD_LIGHT_DUMP_RESPONSE((short) 751);

    private final short code;

    private static final IntHashMap<CommandType> COMMAND_TYPES = buildCommandTypes();

    private static IntHashMap<CommandType> buildCommandTypes() {
        IntHashMap<CommandType> map = new IntHashMap<>();
        for (CommandType value : CommandType.values()) {
            map.put(value.getCode(), value);
        }
        return map;
    }


    CommandType(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static CommandType getType(short code) {
        return switch (code) {
            case 320 -> RESULT;
            case 700 -> TRANSFER;
            case 701 -> TRANSFER_RESPONSE;
            case 710 -> ECHO;
            case 720 -> THREAD_DUMP;
            case 721 -> THREAD_DUMP_RESPONSE;
            case 730 -> ACTIVE_THREAD_COUNT;
            case 731 -> ACTIVE_THREAD_COUNT_RESPONSE;
            case 740 -> ACTIVE_THREAD_DUMP;
            case 741 -> ACTIVE_THREAD_DUMP_RESPONSE;
            case 750 -> ACTIVE_THREAD_LIGHT_DUMP;
            case 751 -> ACTIVE_THREAD_LIGHT_DUMP_RESPONSE;
            default -> null;
        };
    }

}
