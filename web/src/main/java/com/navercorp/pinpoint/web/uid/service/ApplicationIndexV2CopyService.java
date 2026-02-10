package com.navercorp.pinpoint.web.uid.service;

public interface ApplicationIndexV2CopyService {

    void copyApplication();

    void copyAgentId(int durationDays, int maxIterations, int batchSize);

    void copyAgentId(int serviceTypeCode);

    void copyAgentId(String applicationName);
}
