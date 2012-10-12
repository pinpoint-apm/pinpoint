package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.span.SpanAlign;

import java.util.List;

/**
 *
 */
public interface SpanService {
    List<SpanAlign> selectSpan(String uuid);
}
