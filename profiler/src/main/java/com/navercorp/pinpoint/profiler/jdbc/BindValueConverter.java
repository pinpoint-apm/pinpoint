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

package com.navercorp.pinpoint.profiler.jdbc;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class BindValueConverter {

    static final int DEFAULT_ABBREVIATE_MAX_WIDTH = 32;

    private final int maxWidth;
    private final Map<String, Converter> converterMap = new HashMap<>();

    public static BindValueConverter defaultBindValueConverter() {
        return defaultBindValueConverter(DEFAULT_ABBREVIATE_MAX_WIDTH);
    }

    public static BindValueConverter defaultBindValueConverter(int maxWidth) {
        final BindValueConverter converter = new BindValueConverter(maxWidth);
        converter.simpleType();
        converter.classNameType();

        converter.setNullConverter();

        converter.setHexBytesConverter();

        converter.setObjectConverter();
        return converter;
    }


    public BindValueConverter() {
        this(DEFAULT_ABBREVIATE_MAX_WIDTH);
    }

    public BindValueConverter(int maxWidth) {
        this.maxWidth = maxWidth;
        Assert.isTrue(maxWidth > 0, "negative abbreviateMaxWidth");
    }

    private void register(String methodName, Converter converter) {
        this.converterMap.put(methodName, converter);
    }

    private void classNameType() {
        // replace with class name if we don't want to (or can't) read the value
        ClassNameConverter classNameConverter = new ClassNameConverter();

        // There also is method with 3 parameters.
        this.register("setAsciiStream", classNameConverter);
        this.register("setUnicodeStream", classNameConverter);
        this.register("setBinaryStream", classNameConverter);

        // There also is method with 3 parameters.
        this.register("setBlob", classNameConverter);
        // There also is method with 3 parameters.
        this.register("setClob", classNameConverter);
        this.register("setArray", classNameConverter);
        this.register("setNCharacterStream", classNameConverter);

        // There also is method with 3 parameters.
        this.register("setNClob", classNameConverter);

        this.register("setCharacterStream", classNameConverter);
        this.register("setSQLXML", classNameConverter);
    }

    public void setRawBytesConverter() {
        this.register("setBytes", new BytesConverter(this.maxWidth));
    }

    public void setHexBytesConverter() {
        this.register("setBytes", new HexBytesConverter(this.maxWidth));
    }

    private void setObjectConverter() {
        this.register("setObject", new ObjectConverter(maxWidth));
    }

    private void setNullConverter() {
        // There also is method with 3 parameters.
        this.register("setNull", new NullTypeConverter());
    }

    private void simpleType() {

        SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter(maxWidth);

        this.register("setByte", simpleTypeConverter);
        this.register("setBoolean", simpleTypeConverter);
        this.register("setShort", simpleTypeConverter);
        this.register("setInt", simpleTypeConverter);
        this.register("setLong", simpleTypeConverter);
        this.register("setFloat", simpleTypeConverter);
        this.register("setDouble", simpleTypeConverter);
        this.register("setBigDecimal", simpleTypeConverter);
        this.register("setString", simpleTypeConverter);
        this.register("setDate", simpleTypeConverter);

        // There also is method with 3 parameters.
        this.register("setTime", simpleTypeConverter);
        //this.register("setTime", simpleTypeConverter);

        // There also is method with 3 parameters.
        this.register("setTimestamp", simpleTypeConverter);
        //this.register("setTimestamp", simpleTypeConverter);


        // could be replaced with string
        this.register("setURL", simpleTypeConverter);
        // could be replaced with string
        this.register("setRef", simpleTypeConverter);
        this.register("setNString", simpleTypeConverter);
    }


    public String convert(String methodName, Object[] args) {
        final Converter converter = this.converterMap.get(methodName);
        if (converter == null) {
            return "";
        }
        return converter.convert(args);
    }

}
