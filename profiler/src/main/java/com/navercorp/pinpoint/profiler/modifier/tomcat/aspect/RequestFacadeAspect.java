package com.nhn.pinpoint.profiler.modifier.tomcat.aspect;

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.profiler.interceptor.aspect.Aspect;
import com.nhn.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;
import org.apache.catalina.connector.RequestFacade;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * @author emeroad
 */
@Aspect
public abstract class RequestFacadeAspect {

	@PointCut
	public String getHeader(String name) {
		if (Header.isHeaderName(name)) {
			return null;
		}
		return __getHeader(name);
	}

	@JointPoint
	abstract String __getHeader(String name);


}
