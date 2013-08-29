package com.nhn.pinpoint.collector.regionsplit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix.OneByteSimpleHash;

public class PreSplitRegionsTest {

	int traceRegionNumber = 64;
	int traceIndexRegionNumber = 32;
	int agentStatRegionNumber = 32;

	String tracesFormat = "create 'Traces', { NAME => 'S', TTL => 604800  }, { NAME => 'A', TTL => 604800  }, { NAME => 'T', TTL => 604800  }, %s";
	String traceIndexFormat = "create 'ApplicationTraceIndex', { NAME => 'I', TTL => 604800  }, %s";
	String agentStatFormat = "create 'AgentStat', { NAME => 'S', TTL => 604800  }, %s";
	
	@Test
	public void traceRegions() {
		List<String> regions = new ArrayList<String>();
		
		int width = 256 / traceRegionNumber;
		for (int onebyte=width; onebyte<256; onebyte+=width) {
			String region = "\\x" +  Integer.toString((onebyte & 0xff) + 0x100, 16).substring(1);
			for (int i = 0; i < 15; i++) {
				region += "\\x00";
			}
			System.out.println(region);
			regions.add(region);
		}
		
		printCommand(tracesFormat, regions);
	}
	
	@Test
	public void applicationTraceIndexRegions() {
		List<String> regions = new ArrayList<String>();
		
		OneByteSimpleHash hash = new OneByteSimpleHash(traceIndexRegionNumber);
		for (byte[] each : hash.getAllPossiblePrefixes()) {
			byte onebyte = each[0];
			if (onebyte == 0) {
				continue;
			}
			String region = "\\x" +  Integer.toString((onebyte & 0xff) + 0x100, 16).substring(1);
			for (int i = 0; i < 15; i++) {
				region += "\\x00";
			}
			regions.add(region);
		}
		
		printCommand(traceIndexFormat, regions);
	}
	
	@Test
	public void agentStatRegions() {
		List<String> regions = new ArrayList<String>();
		
		OneByteSimpleHash hash = new OneByteSimpleHash(agentStatRegionNumber);
		for (byte[] each : hash.getAllPossiblePrefixes()) {
			byte onebyte = each[0];
			if (onebyte == 0) {
				continue;
			}
			String region = "\\x" +  Integer.toString((onebyte & 0xff) + 0x100, 16).substring(1);
			for (int i = 0; i < 15; i++) {
				region += "\\x00";
			}
			regions.add(region);
		}
		
		printCommand(agentStatFormat, regions);
	}
	
	void printCommand(String format, List<String> regions) {
		StringBuilder sb = new StringBuilder();
		sb.append("{SPLITS=>[");
		for (int i=0; i<regions.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("\"").append(regions.get(i)).append("\"");
		}
		sb.append("]}");
		
		System.out.println(String.format(format, sb.toString()));
	}
}
