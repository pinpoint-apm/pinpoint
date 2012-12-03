package com.profiler.common.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author netspider
 * 
 */
public class TerminalStatisticsBo implements Serializable {

	private static final long serialVersionUID = -2715236149783596830L;

	private final int histogramResolution;
	private final Set<String> agentIds = new HashSet<String>();
	private final HistogramBo histogram;

	public TerminalStatisticsBo() {
		histogramResolution = HistogramBo.DEFAULT_RESOLUTION;
		histogram = new HistogramBo(histogramResolution);
	}

	public void sampleElapsedTime(int value) {
		histogram.addSample(value);
	}

	public void addAgentId(String agentId) {
		agentIds.add(agentId);
	}

	public Set<String> getAgentIds() {
		return agentIds;
	}

	public HistogramBo getHistogram() {
		return histogram;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("AgentIds=").append(agentIds);
		sb.append(", Histogram=").append(histogram);

		return sb.toString();
	}

	/**
	 * TODO SpanBo와 같이 readbytes, writebytes로 변경이 필요해보임.
	 * 
	 * @return
	 */
	public byte[] toBytes() {
		ByteArrayOutputStream bos = null;
		ObjectOutput out = null;
		try {
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
			out.writeObject(this);
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (bos != null)
					bos.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static TerminalStatisticsBo parse(byte[] bytes) {
		ByteArrayInputStream bis = null;
		ObjectInput in = null;
		try {
			bis = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bis);
			return (TerminalStatisticsBo) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (bis != null)
					bis.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
