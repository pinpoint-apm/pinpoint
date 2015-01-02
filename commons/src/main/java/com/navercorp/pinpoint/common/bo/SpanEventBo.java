/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.bo;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.*;

/**
 * @author emeroad
 */
public class SpanEventBo implements Span {
    private static final int VERSION_SIZE = 1;
   // version 0 means that the type of prefix's size is in

	private byte version      0;

	private String agentId;
    private String applicationId;
    private long agentStartTime;

    private String trace    gentId;
	private long traceAgen    StartTime;
	private long traceTransac    ionSequence;

	pri    ate long spanId;
	priv    te short sequence;

	pr    vate int startElapsed;    	private int endE    apsed;

	private String rpc;
	private ServiceType serviceType;

       private String destinationId;
	private Strin     endPoint;
    private int apiId;

	private    List<AnnotationBo> an    otationBoList;

	private int depth = -1;
	private long nextSpanId = -1;

    private boolean hasException;
    private int exceptionId;
    private String exceptionMessage;

    // should get exceptionClass f    om dao
    private S        ing exceptionClass;


	public SpanEventBo() {
	}

	public SpanEventBo(TSpan tSpan, TSpanEvent tSpanEvent) {
        if (tSpan == null) {
            throw new NullPointerException("tSpan must not be null");
        }
        if (tSpanEvent == null) {
            throw new NullPointerException("tSpanEvent must not be null");
        }

        this.agentId = tSpan.getAgentId();
        this.applicationId = tSpan.getApplicationName();
        this.agentStartTime = tSpan.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(tSpan.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
        this.traceAgentStartTime = transactionId.getAgentStartTime();
        this.traceTransacti       nSequence = transactionId.ge       TransactionSequence();

		this.spanId         tSpan.getSpanId();
		this.sequence = tSpanEv       nt.getSequence();

		this.startElapsed = t       panEvent.getStartElapsed();       		this.endElapsed = tSpanEvent.getEndElapsed();

		this.rpc = tSpanEvent.getRpc();
		this.serviceType = ServiceType.findServiceType(tSpanEvent.getServiceType());


        this.destinationId = tSpanEvent.getDestinationId();

                    this.endPoint = tSpa          Event.getEndPoint();
               his.ap       Id = tSpanEvent.getApiId();
		
          	if (tSpanEvent.isSetDepth()) {
			this       depth         tSpanEvent.getDepth();
		}
        
		if (tSpanEvent.isSetNextSpanId()) {
			this.nextSpanId = tSpanEvent.getNextSpanId();
		}
        
		setAnnotationBoList(tSpanEvent.getAnnotations());

        final TIntStringValue exceptionInfo = tSpanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            this.hasException =         ue;
            this.exceptionId = exceptionInfo.getIntValue();
            this.exceptionMessage = exceptionInfo.getStringValue();
        }
	}

	public SpanEventBo(TSpanChunk spanChunk, TSpanEvent spanEvent) {
        if (spanChunk == null) {
            throw new NullPointerException("spanChunk must not be null");
        }
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }

        this.agentId = spanChunk.getAgentId();
        this.applicationId = spanChunk.getApplicationName();
        this.agentStartTime = spanChunk.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(spanChunk.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
              this.traceAgentStartTime =       transactionId.getAgentStartTime();
              this.traceTransactionSequence = transac       ionId.getTransactionSequence();

		this.s       anId = spanChunk.getSpanId       );
		this.sequence = spanEvent.getSequence();

		this.startElapsed = spanEvent.getStartElapsed();
		this.endElapsed = spanEvent.getE       dElapsed();

		this.rpc = spanEvent.getRpc();
		this.serviceType = ServiceType.             indServiceType(spanEve          t.getServiceType());

                    this.destinationId = spanEve          t.getDestinationId();

		this.endPoint                   = spanEvent.getEndPoint();
        this.apiId = spanEvent.getApiId();
		
		if (spanEvent.isSetDepth()) {
			this.depth = spanEvent.getDepth();
		}

		if (spanEvent.isSetNextSpanId()) {
			this.nextSpanId = spanEvent.getNextSpanId();
		}
		
		setAnnotationBoList(spanEvent.getAnnotations());

        final TIntStringValue excepti    nI    fo = spanEvent.getExcept       onInfo();
              if (exceptionInfo != null) {
                  this.hasEx        ption = true;
                   his.excepti        Id = exceptionInfo.getIntValue();
                  this.excepti    nMessage = exceptionInfo.getStringValue();
        }
	}



	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

    public long getAgentStartTime() {
        return this.agentStartTime;
    }

    public void setAgentStar       Time(long agentStartTim         {
        this.agentStartTime = agentStartTime;
    }

           ublic String getTraceAgentId() {
        re        rn traceAgentId;
    }

    public void s       tTraceAgentId(String traceAg        tId) {
        this.traceAgentId = traceAgentId;
    }

    public lon        getTraceAgentStartTime() {
		return traceAgentStartT        e;
	}

	public void setTraceAgentS       artTime(long trac        gentStartTime) {
		this       traceAgentStart        me = traceAgentStartTime;
       }

	public l        g getTraceTransactionSequence() {
		ret       rn traceTransactionSe        ence;
	}

	public void setTr       ceTransactionSeq        nce(long traceTransactionSequence) {
		this.t       aceTransactionSequence = trac        ransactionSequence;
	}

	p       blic void setS        nId(long spanId) {
		this.spanId = spanId
	}

	public long getSpan        () {
		return this.spa       Id;
	}
        public short getSequence() {
	       return sequ        ce;
	}

	public void setSequence(sh       rt sequence) {
        this.sequence = sequence;
	}

	public int getStartE       apsed() {
		return startEla        ed;
	}

	public void setSta       tElapsed(int        tartElapsed) {
		this.startElapsed = sta       tElapsed;
	}

	public    int getEndElapsed() {
		return endElapsed;
	}

	public void setEndElapsed(int endElapsed) {
		this.endElapsed = endElapsed;
	}

	public String getRpc() {
		return rpc;
	}

	public void setRpc(String rpc) {
		this.rpc = rpc;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType =        erviceType;
	}

	pub        c String getEndPoint(        {
		retu         endPoint;
	}

	public void set       ndPoint(String         dPoint) {
		this.endPoint =       endPoint;
	}

          public int getApiId() {
        return a       iId;
    }

    public vo         setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getDestinationId() {
        return destinationId;
          }

    public void setDestinationId(String destinationId) {
        th       s.destinationId = destinationId;
             }


    public List<Annot             tionBo> getAnnotationBoLi    t() {
		return annotationBoList;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public long getNextSpanId() {
		return nextSpanId;
	}

	public void setNextSpanId(long nextSpanId) {
		this.nextSpanId = nextSpanId;
	}

	private void setAnnotationBoList(List<TAnnotation> annotations) {
        if (annotations == null) {
            return;
        }
		List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
		for (TAnnotation ano : annotations) {
			boList.add(new AnnotationBo(ano));
		}
		this.annotationBoList = boList;
	}

    public boolean hasException() {
        return hasException;
    }

    public int getExceptionId() {
        return exceptionId;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }



    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer(512);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.putPrefixedString(agentId);
        buffer.putPrefixedString(applicationId);
        buffer.putVar(agentStartTime);

        buffer.putVar(startElapsed);
        buffer.putVar(endElapsed);

        // don't need to put sequence because it is set at Qualifier
        // buffer.put(sequence);

        buffer.putPrefixedString(rpc);
        buffer.put(serviceType.getCode());
        buffer.putPrefixedString(endPoint);
              buffer.putPrefixedString(destinationId);
        buffer.putSVar(apiId);

        buffer.putSVar(depth);
               uffer.put(nextSpanId);

              if (hasException) {
            buf       er.put(true);
            buffer.putSV       r(exceptionId);
            buffer.putPrefixedString(exceptionMessage);
        } else {
            buffer.put(false);
        }

        writeAnnota       ion(buffer);


        return buffer       getBuffer();
    }



    private void writeAnnotation(Buffer buffer) {
        AnnotationBoList annotationB        = new AnnotationBoList(this.annotat       onBoList);
        annotationBo.wri       eValue(buffer);
	}


	public int readValue(byte[] bytes, int of       set) {
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset);

		this.version = buffer.readByte();

		// this.mostTraceID = buffe       .readLong();
		// this.leastTr       ceID = buffer.readLong();

		this.agentId = buffer.readPrefixedString();
        this.applicationId = buffer.readPrefixedString();
        this.agentStartTime = buffer.readVarLong();

		this.startElapsed = buffer.readVarInt();
		this.endEl             psed = buffer.readVarInt();

        //        on't need to get seque        e because it can be got at Qualifier
		// this.sequence = buffer.readShort();


		this.rpc = buffer.readPrefixedString();
		this.serviceType = ServiceType.findServiceTy       e(buffer.readShort());
		this.endPoint = b    ffer.readPrefixedString();
        this.destinationId = buffer.readPrefixedString();
        this.apiId = buffer.readSVarInt();

		this.depth = buffer.readSVarInt();
		this.nextSpanId = buffer.readLong();

        this.hasException = buffer.readBoolean();
        if (hasException) {
            this.exceptionId = buffer.readSVarInt();
            this.exceptionMessage = buffer.readPrefixedString();
        }
		
		this.annotationBoList = readAnnotation(buffer);
		return buffer.getOffset();
	}

	private List<AnnotationBo> readAnnotation(Buffer buffer) {
        AnnotationBoList annotationBoList = new AnnotationBoList();
        annotationBoList.readValue(buffer);
		return annotationBoList.getAnnotationBoList();
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("SpanEventBo{");
        sb.append("version=").append(version);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", traceAgentId='").append(traceAgentId).append('\'');
        sb.append(", traceAgentStartTime=").append(traceAgentStartTime);
        sb.append(", traceTransactionSequence=").append(traceTransactionSequence);
        sb.append(", spanId=").append(spanId);
        sb.append(", sequence=").append(sequence);
        sb.append(", startElapsed=").append(startElapsed);
        sb.append(", endElapsed=").append(endElapsed);
        sb.append(", rpc='").append(rpc).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", destinationId='").append(destinationId).append('\'');
        sb.append(", endPoint='").append(endPoint).append('\'');
        sb.append(", apiId=").append(apiId);
        sb.append(", annotationBoList=").append(annotationBoList);
        sb.append(", depth=").append(depth);
        sb.append(", nextSpanId=").append(nextSpanId);
        sb.append(", hasException=").append(hasException);
        sb.append(", exceptionId=").append(exceptionId);
        sb.append(", exceptionMessage='").append(exceptionMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
