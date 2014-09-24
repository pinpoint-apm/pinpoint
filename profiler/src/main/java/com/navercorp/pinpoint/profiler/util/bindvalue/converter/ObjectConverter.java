package com.nhn.pinpoint.profiler.util.bindvalue.converter;

import com.nhn.pinpoint.profiler.util.ArrayUtils;
import com.nhn.pinpoint.bootstrap.util.StringUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @author emeroad
 */
public class ObjectConverter implements Converter {
    @Override
    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            Object param = args[1];
            return getParameter(param);

        } else if (args.length == 3) {
            Object param = args[1];
            return getParameter(param);
        }
        return "error";
    }

    private String getParameter(Object param) {
        if(param == null) {
            return "null";
        } else {
            if (param instanceof Byte) {
				return dropToString(param);
			} else if (param instanceof String) {
				return StringUtils.drop((String) param);
			} else if (param instanceof BigDecimal) {
				return dropToString(param);
			} else if (param instanceof Short) {
				return dropToString(param);
			} else if (param instanceof Integer) {
				return dropToString(param);
			} else if (param instanceof Long) {
				return dropToString(param);
			} else if (param instanceof Float) {
				return dropToString(param);
			} else if (param instanceof Double) {
				return dropToString(param);
			} else if (param instanceof BigInteger) {
				return dropToString(param);
			} else if (param instanceof java.sql.Date) {
				return dropToString(param);
			} else if (param instanceof Time) {
				return dropToString(param);
			} else if (param instanceof Timestamp) {
				return dropToString(param);
			} else if (param instanceof Boolean) {
				return dropToString(param);
			} else if (param instanceof byte[]) {
                return ArrayUtils.dropToString((byte[]) param);
			} else if (param instanceof InputStream) {
				return getClassName(param);
			} else if (param instanceof java.sql.Blob) {
				return getClassName(param);
			} else if (param instanceof java.sql.Clob) {
				return getClassName(param);
			} else {
				return getClassName(param);
			}
        }
    }

    private String dropToString(Object param) {
        return StringUtils.drop(param.toString());
    }

    private String getClassName(Object param) {
        return param.getClass().getName();
    }
}
