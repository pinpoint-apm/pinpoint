package com.nhn.pinpoint.rpc.packet;

public enum HandShakeResponseCode {

    SUCCESS(0, 0, "Success."), 
    SIMPLEX_COMMUNICATION(0, 1, "Simplex Connection successfully established."), 
    DUPLEX_COMMUNICATION(0, 2, "Duplex Connection successfully established."),

    ALREADY_KNOWN(1, 0, "Already Known."),
    ALREADY_SIMPLEX_COMMUNICATION(1, 1, "Already Simplex Connection eastablished."), 
    ALREADY_DUPLEX_COMMUNICATION(1, 2, "Already Duplex Connection established."),
    
    PROPERTY_ERROR(2, 0, "Property error."),

    PROTOCOL_ERROR(3, 0, "Illegal protocol error."),

    UNKNOWN_ERROR(4, 0, "Unkown Error."),

    UNKOWN_CODE(-1, -1, "Unkown Code.");

    private final int code;
    private final int subCode;
    private final String codeMessage;

    private HandShakeResponseCode(int code, int subCode, String codeMessage) {
        this.code = code;
        this.subCode = subCode;
        this.codeMessage = codeMessage;
    }

    public int getCode() {
        return code;
    }

    public int getSubCode() {
        return subCode;
    }

    public String getCodeMessage() {
        return codeMessage;
    }

    public static HandShakeResponseCode getValue(int code, int subCode) {
        for (HandShakeResponseCode value : HandShakeResponseCode.values()) {
            if (code != value.getCode()) {
                continue;
            }

            if (subCode != value.getSubCode()) {
                continue;
            }

            return value;
        }
        
        for (HandShakeResponseCode value : HandShakeResponseCode.values()) {
            if (code != value.getCode()) {
                continue;
            }

            if (0 != value.getSubCode()) {
                continue;
            }

            return value;
        }

        return UNKOWN_CODE;
    }

}
