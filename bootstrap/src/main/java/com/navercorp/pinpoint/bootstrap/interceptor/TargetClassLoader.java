package com.nhn.pinpoint.bootstrap.interceptor;

/**
 * 마커 newInterceptor를 통해 new할 경우 마크로 붙여야 한다. new Interceptor()를 하지 않아도 되는 곳에 강제 로딩을 하였을 경우 에러를 발생시키기 위해서 만듬.
 * @author emeroad
 */
public interface TargetClassLoader {
}
