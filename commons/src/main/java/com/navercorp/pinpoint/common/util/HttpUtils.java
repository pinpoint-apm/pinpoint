package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public class HttpUtils {

    private static final String UTF8 = "UTF-8";

    private static final String CHARSET = "charset=";

    public static String parseContentTypeCharset(String contentType) {
        return parseContentTypeCharset(contentType, UTF8);
    }

    public static String parseContentTypeCharset(String contentType, String defaultCharset) {
        if (contentType == null) {
            // 스펙상으로는 iso-8859-1 이나 요즘 대부분 was에서 UTF-8 고치기 때문에 애매하다. 옵션 설정에서 고칠수 있게 해야 될지도 모름.
            return defaultCharset;
        }
        int charsetStart = contentType.indexOf(CHARSET);
        if (charsetStart == -1) {
            // 없음.
            return defaultCharset;
        }
        // 요기가 시작점.
        charsetStart = charsetStart + CHARSET.length();
        int charsetEnd = contentType.indexOf(';', charsetStart);
        if (charsetEnd == -1) {
            charsetEnd = contentType.length();
        }
        contentType = contentType.substring(charsetStart, charsetEnd);

        return contentType.trim();
    }
}
