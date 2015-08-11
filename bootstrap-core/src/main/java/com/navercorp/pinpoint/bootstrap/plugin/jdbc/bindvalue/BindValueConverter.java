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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import java.util.HashMap;
import java.util.Map;

public class BindValueConverter {
    private static final BindValueConverter converter;
    static {
        converter = new BindValueConverter();
        converter.register();
    }

    public final Map<String, Converter> convertermap = new HashMap<String, Converter>() ;

    private void register() {
        simpleType();
        classNameType();

        // There also is method with 3 parameters.
        convertermap.put("setNull", new NullTypeConverter());

        BytesConverter bytesConverter = new BytesConverter();
        convertermap.put("setBytes", bytesConverter);

        convertermap.put("setObject", new ObjectConverter());
    }

    private void classNameType() {
        // replace with class name if we don't want to (or can't) read the value
        ClassNameConverter classNameConverter = new ClassNameConverter();
        
     // There also is method with 3 parameters.
        convertermap.put("setAsciiStream", classNameConverter);
        convertermap.put("setUnicodeStream", classNameConverter);
        convertermap.put("setBinaryStream", classNameConverter);

     // There also is method with 3 parameters.
        convertermap.put("setBlob", classNameConverter);
     // There also is method with 3 parameters.
        convertermap.put("setClob", classNameConverter);
        convertermap.put("setArray", classNameConverter);
        convertermap.put("setNCharacterStream", classNameConverter);

     // There also is method with 3 parameters.
        convertermap.put("setNClob", classNameConverter);

        convertermap.put("setCharacterStream", classNameConverter);
        convertermap.put("setSQLXML", classNameConverter);
    }

    private void simpleType() {

        SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
        convertermap.put("setByte", simpleTypeConverter);
        convertermap.put("setBoolean", simpleTypeConverter);
        convertermap.put("setShort", simpleTypeConverter);
        convertermap.put("setInt", simpleTypeConverter);
        convertermap.put("setLong", simpleTypeConverter);
        convertermap.put("setFloat", simpleTypeConverter);
        convertermap.put("setDouble", simpleTypeConverter);
        convertermap.put("setBigDecimal", simpleTypeConverter);
        convertermap.put("setString", simpleTypeConverter);
        convertermap.put("setDate", simpleTypeConverter);

     // There also is method with 3 parameters.
        convertermap.put("setTime", simpleTypeConverter);
        //convertermap.put("setTime", simpleTypeConverter);

     // There also is method with 3 parameters.
        convertermap.put("setTimestamp", simpleTypeConverter);
        //convertermap.put("setTimestamp", simpleTypeConverter);


        // could be replaced with string
        convertermap.put("setURL", simpleTypeConverter);
        // could be replaced with string
        convertermap.put("setRef", simpleTypeConverter);
        convertermap.put("setNString", simpleTypeConverter);
    }

    public String convert0(String methodName, Object[] args) {
        Converter converter = this.convertermap.get(methodName);
        if (converter == null) {
            return "";
        }
        return converter.convert(args);
    }


    public static String convert(String methodName, Object[] args) {
        return converter.convert0(methodName, args);
    }

}
