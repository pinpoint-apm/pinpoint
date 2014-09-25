package com.nhn.pinpoint.profiler.util.bindvalue;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.profiler.util.bindvalue.converter.*;
import com.nhn.pinpoint.profiler.util.bindvalue.converter.NullTypeConverter;

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

        // null argument 가 3개인것도 있음.
        convertermap.put("setNull", new NullTypeConverter());

        BytesConverter bytesConverter = new BytesConverter();
        convertermap.put("setBytes", bytesConverter);

        // setObject
        convertermap.put("setObject", new ObjectConverter());
    }

    private void classNameType() {
        // className  데이터를 까볼수 없는 객체의 경우 class명으로 치환
        ClassNameConverter classNameConverter = new ClassNameConverter();
        // 3개짜리 존재
        convertermap.put("setAsciiStream", classNameConverter);
        convertermap.put("setUnicodeStream", classNameConverter);
        convertermap.put("setBinaryStream", classNameConverter);

        //3개 짜리 존재
        convertermap.put("setBlob", classNameConverter);
        //3개 짜리 존재
        convertermap.put("setClob", classNameConverter);
        convertermap.put("setArray", classNameConverter);
        convertermap.put("setNCharacterStream", classNameConverter);

        // 3개 짜리 존재
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

        // argument 가 3개 가능.
        convertermap.put("setTime", simpleTypeConverter);
        //convertermap.put("setTime", simpleTypeConverter);

        // argument 가 3개 가능
        convertermap.put("setTimestamp", simpleTypeConverter);
        //convertermap.put("setTimestamp", simpleTypeConverter);


        // 문자열로 치환 가능할것으로 보임.
        convertermap.put("setURL", simpleTypeConverter);
        // ref도 문자열로 치환 가능할것으로 보임
        convertermap.put("setRef", simpleTypeConverter);
        convertermap.put("setNString", simpleTypeConverter);
    }

    public String convert0(String methodName, Object[] args) {
        Converter converter = this.convertermap.get(methodName);
        if (converter == null) {
            // Converter를 찾지 못했을 경우 "" 빈문자열을 리턴하도록 한다.
            return "";
        }
        return converter.convert(args);
    }


    public static String convert(String methodName, Object[] args) {
        return converter.convert0(methodName, args);
    }

}
