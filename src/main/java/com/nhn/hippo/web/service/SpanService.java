package com.nhn.hippo.web.service;

import java.util.List;

/**
 *
 */
public interface SpanService {
    List<SpanAlign> selectSpan(String uuid);
}
