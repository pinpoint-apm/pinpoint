package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.LinkKey;

/**
 * 호출관계의 양방향 데이터를 표현
 * @author emeroad
 */
public class RawCallData {

    private final String source;
    private final ServiceType sourceServiceType;

	private final String target;
	private final ServiceType targetServiceType;

	private final Histogram targetHistogram;

    public RawCallData(LinkKey linkKey) {
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        this.source = linkKey.getFromApplication();
        this.sourceServiceType = linkKey.getFromServiceType();

        this.target = linkKey.getToApplication();
        this.targetServiceType = linkKey.getToServiceType();

        this.targetHistogram = new Histogram(targetServiceType);

    }

//	public RawCallData(String source, ServiceType sourceServiceType, String target, ServiceType targetServiceType) {
//        if (source == null) {
//            throw new NullPointerException("source must not be null");
//        }
//        if (sourceServiceType == null) {
//            throw new NullPointerException("sourceServiceType must not be null");
//        }
//        if (target == null) {
//            throw new NullPointerException("target must not be null");
//        }
//        if (targetServiceType == null) {
//            throw new NullPointerException("targetServiceType must not be null");
//        }
//        this.source = source;
//        this.sourceServiceType = sourceServiceType;
//
//        this.target = target;
//		this.targetServiceType = targetServiceType;
//
//		this.targetHistogram = new Histogram(targetServiceType);
//	}
//
//    public RawCallData(RawCallData rawCallData) {
//        if (rawCallData == null) {
//            throw new NullPointerException("rawCallData must not be null");
//        }
//
//        this.source = rawCallData.source;
//        this.sourceServiceType = rawCallData.sourceServiceType;
//
//        this.target = rawCallData.target;
//        this.targetServiceType = rawCallData.targetServiceType;
//
//        this.targetHistogram = new Histogram(targetServiceType);
//        this.targetHistogram.add(rawCallData.targetHistogram);
//    }

	public Histogram getHistogram() {
		return targetHistogram;
	}


}
