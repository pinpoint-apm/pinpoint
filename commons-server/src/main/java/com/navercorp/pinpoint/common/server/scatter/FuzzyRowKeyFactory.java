package com.navercorp.pinpoint.common.server.scatter;

import java.util.List;

public interface FuzzyRowKeyFactory<T> {

    T getKey(long timeStamp);

    List<T> getRangeKey(long high, long low);

}
