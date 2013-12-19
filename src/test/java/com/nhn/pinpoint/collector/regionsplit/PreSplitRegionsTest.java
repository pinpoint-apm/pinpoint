package com.nhn.pinpoint.collector.regionsplit;

import java.util.ArrayList;
import java.util.List;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.junit.Test;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix.OneByteSimpleHash;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class PreSplitRegionsTest {

    int metaDataNumber = 8;
	int traceRegionNumber = 64;
	int traceIndexRegionNumber = 32;
	int agentStatRegionNumber = 32;

    String metadata = "%s";
	String tracesFormat = "create 'Traces', { NAME => 'S', TTL => 604800  }, { NAME => 'A', TTL => 604800  }, { NAME => 'T', TTL => 604800  }, %s";
	String traceIndexFormat = "create 'ApplicationTraceIndex', { NAME => 'I', TTL => 604800  }, %s";
	String agentStatFormat = "create 'AgentStat', { NAME => 'S', TTL => 604800  }, %s";

    @Autowired
    @Qualifier("applicationTraceIndexDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;


    @Test
    public void metaData8() {
        List<String> regions = new ArrayList<String>();

        OneByteSimpleHash hash = new OneByteSimpleHash(metaDataNumber);
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

        printCommand(metadata, regions);
    }
    @Test
	public void traceRegions64() {
        List<String> regions = new ArrayList<String>();

        OneByteSimpleHash hash = new OneByteSimpleHash(traceRegionNumber);
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

        printCommand(tracesFormat, regions);
	}


    @Test
    public void range() {
        System.out.println("------------------------");
        for (int i =0 ; i< 255; i++) {
            byte[] bytes = new byte[1];
            bytes[0] = (byte) i;
            byte[] distributedKey = rowKeyDistributor.getDistributedKey(bytes);
            System.out.println("distributedKey:" + distributedKey[0]);
        }
        System.out.println("------------------------");
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
