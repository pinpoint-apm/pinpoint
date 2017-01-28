/*
 * Copyright 2011-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.ResultScanner;

/**
 * Callback handling scanner results.
 * Implementations of this interface perform the actula work of extracting results from the
 * {@link ResultScanner} but  without having to worry about exception handling or resource management.
 * 
 * @author Costin Leau
 */
public interface ResultsExtractor<T> {

    /**
     * Implementations must implement this method to process the entire {@link ResultScanner}.  
     *  
     * @param results {@link ResultScanner} to extract data from. Implementations should not close this; it will be closed
     * automatically by the calling {@link HbaseTemplate}
     * @return an arbitrary result object, or null if none (the extractor will typically be stateful in the latter case). 
     * @throws Exception if an Hbase exception is encountered
     */
    T extractData(ResultScanner results) throws Exception;
}
