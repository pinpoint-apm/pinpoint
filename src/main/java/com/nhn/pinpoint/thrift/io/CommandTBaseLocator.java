package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDump;

/**
 * 콜렉터에서 프로파일러로 명령을 내릴떄 주고받는 객체들을 선언
 * 여기에 두어야 할지 각 프로파일러, 콜렉터에 둬야 할지는 고민이 좀필요함
 * 
 * @author koo.taejin
 */
public class CommandTBaseLocator implements TBaseLocator  {

    private static final short RESULT = 320;
    private static final Header RESULT_HEADER = createHeader(RESULT);

    private static final short THREAD_DUMP = 720;
    private static final Header THREAD_DUMP_HEADER = createHeader(RESULT);
    
    
	@Override
	public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
	        case RESULT:
	            return new TResult();
	        case THREAD_DUMP:
	            return new TCommandThreadDump();
        }
        throw new TException("Unsupported type:" + type);
	}

	@Override
	public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        if (tbase instanceof TResult) {
            return RESULT_HEADER;
        }
        if (tbase instanceof TCommandThreadDump) {
            return THREAD_DUMP_HEADER;
        }
        
        throw new TException("Unsupported Type" + tbase.getClass());
	}
	
    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }

}
