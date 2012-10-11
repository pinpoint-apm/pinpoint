package com.nhn.hippo.web.dao;


import com.profiler.common.dto.thrift.Span;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public interface TraceDao {

    List<Span> readSpan(UUID uuid);

}
