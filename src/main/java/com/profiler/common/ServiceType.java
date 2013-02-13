package com.profiler.common;

import java.util.HashMap;
import java.util.Map;

import static com.profiler.common.ResponseCode.*;

public enum ServiceType {
	
	UNKNOWN(		(short) 0,		"UNKNOWN",			false, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	UNKNOWN_CLOUD(	(short) 1,		"UNKNOWN_CLOUD",	false, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	
	// TODO internal method를 여기에 넣기 애매하긴 하나.. 일단 그대로 둠.
	INTERNAL_METHOD((short) 2,		"INTERNAL_METHOD",	false, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }), 
	
	TOMCAT(			(short) 1001,	"TOMCAT",			false, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }), 
	BLOC(			(short) 1002,	"BLOC",				false, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	
	MEMCACHED(		(short) 2001,	"MEMCACHED",		true, new short[] { 100, 300, 500 }, new ResponseCode[] { NORMAL, NORMAL, WARN }), 
	ARCUS(			(short) 2002,	"ARCUS",			true, new short[] { 100, 300, 500 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	
	MYSQL(			(short) 3001,	"MYSQL",			true, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	MSSQL(			(short) 3002,	"MSSQL",			true, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	ORACLE(			(short) 3003,	"ORACLE",			true, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	CUBRID(			(short) 3004,	"CUBRID",			true, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN }),
	
	HTTP_CLIENT(	(short) 9001,	"HTTP_CLIENT",		false, new short[] { 1000, 3000, 5000 }, new ResponseCode[] { NORMAL, NORMAL, WARN });
	
	private final short code;
	private final String desc;
	private final boolean terminal;
	private final short[] histogramSlots;				// response time histogram slots
	private final ResponseCode[] histogramDescs;	// slot status code 0:normal, 1:warn, 2:slow
	
	ServiceType(short code, String desc, boolean terminal, short[] histogramSlots, ResponseCode[] histogramDescs) {
		if (histogramSlots.length != histogramDescs.length) {
			throw new IllegalArgumentException();
		}
		this.code = code;
		this.desc = desc;
		this.terminal = terminal;
		this.histogramSlots = histogramSlots;
		this.histogramDescs = histogramDescs;
	}

    public static ServiceType parse(String desc) {
        ServiceType[] values = ServiceType.values();
        for (ServiceType type : values) {
            if (type.desc.equals(desc)) {
                return type;
            }
        }
        return UNKNOWN;
    }


	public boolean isInternalMethod() {
		return code == 2;
	}
	
	public boolean isRpcClient() {
		return code >= 9000 && code < 10000;
	}
	
	public boolean isIndexable() {
		return !terminal && !isRpcClient() && code > 1000;
	}
	
	public boolean isUnknown() {
		return this == ServiceType.UNKNOWN || this == ServiceType.UNKNOWN_CLOUD;
	}
	
	public short getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}

	public boolean isTerminal() {
		return terminal;
	}
	
	public short[] getHistogramSlots() {
		return histogramSlots;
	}

	public ResponseCode[] getHistogramDescs() {
		return histogramDescs;
	}

	@Override
	public String toString() {
		return desc;
	}

    public static ServiceType findServiceType(short code) {
        ServiceType serviceType = CODE_LOOKUP_TABLE.get(code);
        if (serviceType == null) {
            return UNKNOWN;
        }
        return serviceType;
    }


    private static final Map<Short, ServiceType> CODE_LOOKUP_TABLE = new HashMap<Short, ServiceType>();
    static {
        initializeLookupTable();
    }

    public static void initializeLookupTable() {
        ServiceType[] values = ServiceType.values();
        for (ServiceType serviceType : values) {
            ServiceType check = CODE_LOOKUP_TABLE.put(serviceType.code, serviceType);
            if (check != null) {
                throw new IllegalStateException("duplicated code found. code:" + serviceType.code);
            }
        }
    }
}
