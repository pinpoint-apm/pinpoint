package com.nhn.pinpoint.profiler.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public class TBaseBORegistry implements TBaseBOLocator {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<Class<? extends TBase>, TBaseBO> tbaseBORepository;

	public TBaseBORegistry() {
		tbaseBORepository = new HashMap<Class<? extends TBase>, TBaseBO>();
	}

	/**
	 * not guarantee thread safe.
	 */
	public boolean addBO(Class<? extends TBase> clazz, TBaseBO bo) {
		if (tbaseBORepository.containsKey(clazz)) {
			logger.warn("Already Register Type({}).", clazz.getName());
			return false;
		}

		tbaseBORepository.put(clazz, bo);
		return true;
	}

	@Override
	public TBaseBO getBO(TBase tBase) {
		if (tBase == null) {
			throw new NullPointerException("Params may not be null.");
		}

		TBaseBO bo = tbaseBORepository.get(tBase.getClass());
		if (bo != null) {
			return bo;
		}

		List<TBaseBO> candidateHandlerList = new ArrayList<TBaseBO>();
		for (Map.Entry<Class<? extends TBase>, TBaseBO> entry : tbaseBORepository.entrySet()) {
			if (entry.getKey().isInstance(tBase)) {
				candidateHandlerList.add(entry.getValue());
			}
		}

		if (candidateHandlerList.size() == 1) {
			return candidateHandlerList.get(0);
		}
		
		if (candidateHandlerList.size() > 1) {
			logger.warn("Ambigous Pinpoint Handler ({})", candidateHandlerList);
		}
		
		return null;
	}

	@Override
	public TBaseSimpleBO getSimpleBO(TBase tBase) {
		TBaseBO bo = getBO(tBase);

		// null체크 필요없음
		if (bo instanceof TBaseSimpleBO) {
			return (TBaseSimpleBO) bo;
		}

		return null;
	}

	@Override
	public TBaseRequestBO getRequestBO(TBase tBase) {
		TBaseBO bo = getBO(tBase);

		// null체크 필요없음
		if (bo instanceof TBaseRequestBO) {
			return (TBaseRequestBO) bo;
		}

		return null;
	}

}
