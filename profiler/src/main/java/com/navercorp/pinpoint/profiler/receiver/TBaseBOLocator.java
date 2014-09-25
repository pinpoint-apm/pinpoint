package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface TBaseBOLocator {

	TBaseBO getBO(TBase tBase);

	TBaseSimpleBO getSimpleBO(TBase tBase);

	TBaseRequestBO getRequestBO(TBase tBase);

}
