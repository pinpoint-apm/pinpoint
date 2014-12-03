package com.nhn.pinpoint.thrift.io;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

/**
 * @author koo.taejin
 */
public class TCommandRegistry implements TBaseLocator {

	private final ConcurrentHashMap<Short, TCommandType> commandTBaseRepository = new ConcurrentHashMap<Short, TCommandType>();

	public TCommandRegistry(TCommandTypeVersion version) {
		this(version.getSupportCommandList());
	}
	
	public TCommandRegistry(List<TCommandType> supportCommandList) {
		for (TCommandType type : supportCommandList) {
			commandTBaseRepository.put(type.getType(), type);
		}
	}

	@Override
	public TBase<?, ?> tBaseLookup(short type) throws TException {
		TCommandType commandTBaseType = commandTBaseRepository.get(type);
		if (commandTBaseType == null) {
	        throw new TException("Unsupported type:" + type);
		}

		return commandTBaseType.newObject();
	}

	@Override
	public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }

        // 성능 때문에 이거 그냥 먼저 만들어 놓을까? 
        Collection<TCommandType> commandTBaseList = commandTBaseRepository.values();
		
        for (TCommandType commandTBase : commandTBaseList) {
        	if (commandTBase.isInstanceOf(tbase)) {
        		return commandTBase.getHeader();
        	}
        }
        
        throw new TException("Unsupported Type" + tbase.getClass());
	}
	
	@Override
	public boolean isSupport(short type) {
		TCommandType commandTBaseType = commandTBaseRepository.get(type);

		if (commandTBaseType != null) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isSupport(Class<? extends TBase> clazz) {
        // 성능 때문에 이거 그냥 먼저 만들어 놓을까? 
        Collection<TCommandType> commandTBaseList = commandTBaseRepository.values();
		
        for (TCommandType commandTBase : commandTBaseList) {
        	if (commandTBase.getClazz().equals(clazz)) {
        		return true;
        	}
        }
        
        return false;
	}

    @Override
    public Header getChunkHeader() {
        return null;
    }

    @Override
    public boolean isChunkHeader(short type) {
        return false;
    }
}
