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

package com.navercorp.pinpoint.common.server.bo;

import java.util.*;
import com.navercorp.pinpoint.common.server.util.AgentInfoFactory;
import com.navercorp.pinpoint.common.server.util.PassiveAgentInfoFactory;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Peter Chen
 */
public class PassiveSpanBo implements Event {

    private static final int VERSION_SIZE = 1;

    // version 0 means that the type of prefix's size is int
    private byte version = 0;

    private short agentType = 0;

    //  private AgentKeyBo agentKeyBo;
//    private String agentId;
//    private String applicationId;
    private long agentStartTime;

    private TransactionId transactionId;

    private long passiveSpanId;
    private long proxyFrontEndSpanId;
    private long proxyBackEndSpanId;

    private long startTime;
    private int elapsed;

    private String rpc;
    private short serviceType;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> passiveAnnotationBoList = new ArrayList<>();
    private short flag; // optional
    private int errCode;

    private List<SpanEventBo> passiveSpanEventBoList = new ArrayList<>();

    private long collectorAcceptTime;

    private Short applicationServiceType;

    private String acceptorHost;
    private String remoteAddr; // optional

    public PassiveSpanBo() {
    }

    public long getPassiveSpanId() {
        return passiveSpanId;
    }

    public void setPassiveSpanId(long passiveSpanId) {
        this.passiveSpanId = passiveSpanId;
    }


    public int getVersion() {
        return version & 0xFF;
    }


    public byte getRawVersion() {
        return version;
    }

    public void setVersion(int version) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("out of range (0~255)");
        }
        // check range
        this.version = (byte) (version & 0xFF);
    }

    public short getAgentType() {
        return agentType;
    }

    public void setAgentType(short agentType) {
        this.agentType = agentType;
    }

    public TransactionId getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public String getAcceptorHost() {
        return acceptorHost;
    }

    public void setAcceptorHost(String acceptorHost) {
        this.acceptorHost = acceptorHost;
    }

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public List<AnnotationBo> getAnnotationBoList() {
        return passiveAnnotationBoList;
    }

    public void setAnnotationBoList(List<AnnotationBo> anoList) {
        if (anoList == null) {
            return;
        }
        this.passiveAnnotationBoList = anoList;
    }

    public List<SpanEventBo> getPassiveSpanEventBoList() {
        return passiveSpanEventBoList;
    }

    public short getServiceType() {
        return serviceType;
    }

    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    public void setApplicationServiceType(Short applicationServiceType) {
        this.applicationServiceType  = applicationServiceType;
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public boolean hasApplicationServiceType() {
        return applicationServiceType != null;
    }

    public short getApplicationServiceType() {
        if (hasApplicationServiceType()) {
            return this.applicationServiceType;
        } else {
            return this.serviceType;
        }
    }

    public void addPassiveSpanEventBoList(List<SpanEventBo> passiveSpanEventBoList) {
        if (passiveSpanEventBoList == null) {
            return;
        }

        this.passiveSpanEventBoList.addAll(passiveSpanEventBoList);
    }

    public void addPassiveSpanEventBo(SpanEventBo spanEventBo) {
        if (this.passiveSpanEventBoList == null) {
            this.passiveSpanEventBoList = new ArrayList<>();
        }

        this.passiveSpanEventBoList.add(spanEventBo);
    }

    // convert PassiveSpanBo to a fake SpanBo
    public SpanBo createFakeSpanBo() {

        AgentInfoFactory agentInfoFactory = new PassiveAgentInfoFactory();

        long fakeSpanId = this.passiveSpanId;

        SpanBo fakeSpanBo = new SpanBo();
        fakeSpanBo.setSpanId(fakeSpanId);
        fakeSpanBo.setAgentId(agentInfoFactory.createAgentId(this.agentType));
        fakeSpanBo.setParentSpanId(proxyFrontEndSpanId);
        // fakeSpanBo.setAgentId(this.traceAgentId);
        fakeSpanBo.setAgentStartTime(this.agentStartTime);
        fakeSpanBo.setApiId(this.apiId);
        fakeSpanBo.setApplicationId(agentInfoFactory.createApplicationName(this.agentType));
        fakeSpanBo.setApplicationServiceType(this.applicationServiceType);
        fakeSpanBo.setCollectorAcceptTime(this.collectorAcceptTime);
        fakeSpanBo.setTransactionId(this.transactionId);
        fakeSpanBo.setStartTime(this.startTime);
        fakeSpanBo.setElapsed(this.elapsed);
        fakeSpanBo.setEndPoint(this.endPoint);
        fakeSpanBo.setErrCode(this.errCode);
        fakeSpanBo.setFlag(this.flag);
        fakeSpanBo.setRemoteAddr(this.remoteAddr);
        fakeSpanBo.setRpc(this.rpc);
        fakeSpanBo.setServiceType(this.serviceType);
        fakeSpanBo.setVersion(this.version);
        fakeSpanBo.setAcceptorHost(this.acceptorHost);

        if (this.passiveSpanEventBoList != null) {
            fakeSpanBo.addSpanEventBoList(this.passiveSpanEventBoList);
        }

        if (this.passiveAnnotationBoList != null) {
            fakeSpanBo.setAnnotationBoList(this.passiveAnnotationBoList);
        }


        return fakeSpanBo;
    }

    public long getProxyFrontEndSpanId() {
        return proxyFrontEndSpanId;
    }

    public void setProxyFrontEndSpanId(long proxyFrontEndSpanId) {
        this.proxyFrontEndSpanId = proxyFrontEndSpanId;
    }

    public long getProxyBackEndSpanId() {
        return proxyBackEndSpanId;
    }

    public void setProxyBackEndSpanId(long proxyBackEndSpanId) {
        this.proxyBackEndSpanId = proxyBackEndSpanId;
    }

    /**
     * The chain of passiveSpanBos that have same proxyFrontEndSpanId and proxyBackEndSpanId
     * we have to consider this two conditions:
     * 1, Tomcat --> apache1 --> apache2 ... --> apacheN --> tomcat (apache1, apache2... and apacheN make a passiveSpanBoChain)
     * 2, Tomcat --> apache1 --> tomcat (apache1 make a passiveSpanBoChain)
     * The process to create passiveSpanBoChain:
     * e.g. Tomcat1
     *   --> apache1(proxyFrontEndSpanId=1, proxyBackEndSpanId=2, passiveSpanId=11, startTime=101)
     *   --> apache2(proxyFrontEndSpanId=1, proxyBackEndSpanId=2, passiveSpanId=12, startTime=100)
     *   --> Tomcat2
     * 1, sort by startTime [apache1, apache2] -- > [apache2, apache1]
     * 2, set proxyFrontEndSpanId=1 and proxyBackEndSpanId=2
     * 3, createFakeSpanBoChain:
     *   [apache2(f=1, b=2, p=11), apache1(f=1, b=2, p=12)] f:proxyFrontEndSpanId, b:proxyBackEndSpanId, p:passiveSpanId
     *  -->[SpanBo1(parentSpanId=1, nextSpanId=12, spanId=11), SpanBo2(parentSpanId=11, nextSpanId=2, spanId=12)]
     */
    private static class PassiveSpanBoChain {
        private Logger logger = LoggerFactory.getLogger(this.getClass());

        private List<PassiveSpanBo> passiveSpanBoList;
        private List<SpanBo> fakeSpanBoList;
        private long proxyFrontEndSpanId;
        private long proxyBackEndSpanId;
        private long headSpanId;
        private long tailSpanId;

        PassiveSpanBoChain(List<PassiveSpanBo> passiveSpanBoList) {
            if (passiveSpanBoList == null || passiveSpanBoList.isEmpty()) {
                throw new IllegalArgumentException("passiveSpanBoList is empty.");
            }

            this.passiveSpanBoList = passiveSpanBoList;

            final PassiveSpanBo head = this.passiveSpanBoList.get(0);

            // check
            for (PassiveSpanBo bo : this.passiveSpanBoList) {
                if (bo.getProxyFrontEndSpanId() != head.getProxyFrontEndSpanId()) {
                    logger.error("passiveSpanBoList have different parentSpanId. bo.getProxyFrontEndSpanId()={}, head.getProxyFrontEndSpanId={}",
                            bo.getProxyFrontEndSpanId(), head.getProxyFrontEndSpanId());
                    throw new IllegalArgumentException("passiveSpanBoList have different parentSpanId.");
                }

                if (bo.getProxyBackEndSpanId() != head.getProxyBackEndSpanId()) {
                    logger.error("passiveSpanBoList have different nextSpanId. bo.getProxyBackEndSpanId()={}, head.getProxyBackEndSpanId={}",
                            bo.getProxyBackEndSpanId(), head.getProxyBackEndSpanId());
                    throw new IllegalArgumentException("passiveSpanBoList have different nextSpanId.");
                }
            }

            // sort by startTime
            Collections.sort(this.passiveSpanBoList, new Comparator<PassiveSpanBo>(){
                public int compare(PassiveSpanBo arg0, PassiveSpanBo arg1) {
                    return Long.compare(arg0.getStartTime(), arg1.getStartTime());
                }
            });

            this.proxyFrontEndSpanId = head.getProxyFrontEndSpanId();
            this.proxyBackEndSpanId = head.getProxyBackEndSpanId();

            createFakeSpanBoChain();

            final SpanBo headSpanBo = this.fakeSpanBoList.get(0);
            final SpanBo tailSpanBo = this.fakeSpanBoList.get(this.fakeSpanBoList.size() - 1);

            this.headSpanId = headSpanBo.getSpanId();
            this.tailSpanId = tailSpanBo.getSpanId();
        }


        private void createFakeSpanBoChain() {
            this.fakeSpanBoList = new ArrayList<>();

            // convert to fakeSpan and link
            for (int i = 0; i < passiveSpanBoList.size(); i++) {
                PassiveSpanBo passiveSpanBo = passiveSpanBoList.get(i);
                SpanBo fakeSpanBo = passiveSpanBo.createFakeSpanBo();
                if (i < passiveSpanBoList.size() - 1) {
                    // it is not tail
                    for (SpanEventBo spanEventBo : fakeSpanBo.getSpanEventBoList()) {
                        if (spanEventBo.getNextSpanId() == this.proxyBackEndSpanId) {
                            // Use passiveSpanId here, because the i + 1 fakeSpanBo's spanId = the i + 1 PassiveSpanBo's passiveSpanId.
                            // But now the i + 1 fakeSpanBo has not be generated.
                            spanEventBo.setNextSpanId(passiveSpanBoList.get(i + 1).getPassiveSpanId());
                        }
                    }
                }

                if (i > 0) {
                    // it is not head
                    fakeSpanBo.setParentSpanId(passiveSpanBoList.get(i - 1).getPassiveSpanId());
                }

                fakeSpanBoList.add(fakeSpanBo);
            }
        }

        public long getProxyFrontEndSpanId() {
            return this.proxyFrontEndSpanId;
        }

        public long getProxyBackEndSpanId() {
            return this.proxyBackEndSpanId;
        }

        public long getHeadSpanId() {
            return this.headSpanId;
        }

        public long getTailSpanId() {
            return this.tailSpanId;
        }

        public List<SpanBo> getFakeSpanBoList() {
            return fakeSpanBoList;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(1024);
            sb.append("PassiveSpanBoChain{");
            for (SpanBo spanBo : this.fakeSpanBoList) {
                sb.append(spanBo.toString());
            }

            sb.append('}');
            return sb.toString();
        }

    }

    private static boolean isInOneChain(PassiveSpanBo bo1, PassiveSpanBo bo2) {
        return bo1.getProxyFrontEndSpanId() == bo2.getProxyFrontEndSpanId() && bo1.getProxyBackEndSpanId() == bo2.getProxyBackEndSpanId();
    }

    private static List<PassiveSpanBoChain> createPassiveSpanChain(List<PassiveSpanBo> passiveSpanBoList) {
        List<PassiveSpanBoChain> chains = new ArrayList<>();

        // sort by parentSpanId and nextSpanId
        Collections.sort(passiveSpanBoList, new Comparator<PassiveSpanBo>(){
            public int compare(PassiveSpanBo arg0, PassiveSpanBo arg1) {
                int cmp1 = Long.compare(arg0.getProxyFrontEndSpanId(), arg1.getProxyFrontEndSpanId());
                if (cmp1 != 0) {
                    return cmp1;
                } else {
                    return Long.compare(arg0.getProxyBackEndSpanId(), arg1.getProxyBackEndSpanId());
                }

            }
        });

        int start = 0;
        for (int i = 0; i < passiveSpanBoList.size(); i++) {
            if(!isInOneChain(passiveSpanBoList.get(start), passiveSpanBoList.get(i))) {
                final PassiveSpanBoChain chain = new PassiveSpanBoChain(new ArrayList<>(passiveSpanBoList.subList(start, i)));
                chains.add(chain);
                start = i;
            }
        }

        // put the last
        final PassiveSpanBoChain chain = new PassiveSpanBoChain(new ArrayList<>(passiveSpanBoList.subList(start, passiveSpanBoList.size())));
        chains.add(chain);

        return chains;
    }

    /**
     * The function merges passiveSpanBoList to spanBoList. For every passiveSpanBo:
     * 1, create passiveSpanBoChains
     * 2, insert passiveSpanBoChains to spanBoList. Like insert to a List
     *  before: firstSpanBo(spanId=1, nextSpanId=2) --> secondSpanBo(spanId=2, parentSpanId=1)
     *  after: firstSpanBo(spanId=1, nextSpanId=getHeadSpanId()) --> fakeSpanBoList
     *       --> secondSpanBo(spanId=2, parentSpanId=getTailSpanId)
     */
    public static void mergePassiveSpan(List<SpanBo> spanBoList, List<PassiveSpanBo> passiveSpanBoList){

        if (passiveSpanBoList.isEmpty()) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(PassiveSpanBo.class);

        logger.debug("spanBoList={}, passiveSpanBoList={}", spanBoList, passiveSpanBoList);

        Map<Long, SpanBo> spanBoMap = new HashMap<>();

        for (SpanBo spanBo : spanBoList) {
            spanBoMap.put(spanBo.getSpanId(), spanBo);
        }

        List<PassiveSpanBoChain> passiveSpanBoChains = createPassiveSpanChain(passiveSpanBoList);

        for (PassiveSpanBoChain chain : passiveSpanBoChains) {
            SpanBo proxyFrontEndSpanBo = spanBoMap.get(chain.getProxyFrontEndSpanId());
            SpanBo proxyBackEndSpanBo = spanBoMap.get(chain.getProxyBackEndSpanId());

            if (proxyFrontEndSpanBo == null || proxyBackEndSpanBo == null) {
                logger.warn("can not find parentSpanBo={} or nextSpanBo={} for chain={}. ", proxyFrontEndSpanBo, proxyBackEndSpanBo, chain);
                continue;
            }

            for (SpanEventBo spanEventBo : proxyFrontEndSpanBo.getSpanEventBoList()) {
                if (spanEventBo.getNextSpanId() == chain.getProxyBackEndSpanId()) {
                    spanEventBo.setNextSpanId(chain.getHeadSpanId());
                }
            }

            proxyBackEndSpanBo.setParentSpanId(chain.getTailSpanId());

            spanBoList.addAll(chain.getFakeSpanBoList());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("PassiveSpanBo{");
        sb.append("version=").append(version);
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", transactionId='").append(transactionId).append('\'');
        sb.append(", proxyFrontEndSpanId=").append(proxyFrontEndSpanId);
        sb.append(", proxyBackEndSpanId=").append(proxyBackEndSpanId);
        sb.append(", passiveSpanId=").append(passiveSpanId);
        sb.append(", startTime=").append(startTime);
        sb.append(", elapsed=").append(elapsed);
        sb.append(", rpc='").append(rpc).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", endPoint='").append(endPoint).append('\'');
        sb.append(", apiId=").append(apiId);
        sb.append(", passiveAnnotationBoList=").append(passiveAnnotationBoList);
        sb.append(", flag=").append(flag);
        sb.append(", errCode=").append(errCode);
        sb.append(", passiveSpanEventBoList=").append(passiveSpanEventBoList);
        sb.append(", collectorAcceptTime=").append(collectorAcceptTime);
        sb.append(", remoteAddr='").append(remoteAddr).append('\'');
        sb.append(", applicationServiceType=").append(applicationServiceType);
        sb.append('}');
        return sb.toString();
    }


}
