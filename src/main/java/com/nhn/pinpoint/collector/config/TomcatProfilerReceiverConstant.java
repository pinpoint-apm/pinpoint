package com.nhn.pinpoint.collector.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TomcatProfilerReceiverConstant {
	public final static DateFormat DATE_FORMAT_YMD = new SimpleDateFormat("yyyy_MM_dd");
	public final static DateFormat DATE_FORMAT_YMD_HMS = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	public final static DateFormat DATE_FORMAT_YMD_HM = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
	public final static DateFormat DATE_FORMAT_HMS_MS = new SimpleDateFormat("HH:mm:ss,SSS");
}
