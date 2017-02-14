package com.ibm.websphere.servlet.request;

import com.ibm.websphere.servlet.response.IResponse;
//import com.ibm.ws.util.ThreadPool;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.Cookie;

public abstract interface IRequest
{
  public abstract String getMethod();
  
  public abstract String getRequestURI();
  
  public abstract String getRemoteUser();
  
  public abstract String getAuthType();
  
  public abstract String getHeader(String paramString);
  
  public abstract Enumeration getHeaders(String paramString);
  
  public abstract long getDateHeader(String paramString);
  
  public abstract int getIntHeader(String paramString);
  
  public abstract void clearHeaders();
  
  public abstract Enumeration getHeaderNames();
  
  public abstract int getContentLength();
  
  public abstract String getContentType();
  
  public abstract String getProtocol();
  
  public abstract String getServerName();
  
  public abstract int getServerPort();
  
  public abstract String getRemoteHost();
  
  public abstract String getRemoteAddr();
  
  public abstract int getRemotePort();
  
  public abstract String getScheme();
  
  public abstract InputStream getInputStream()
    throws IOException;
  
  public abstract String getLocalAddr();
  
  public abstract String getLocalName();
  
  public abstract int getLocalPort();
  
  public abstract boolean isSSL();
  
  public abstract byte[] getSSLSessionID();
  
  public abstract String getSessionID();
  
  public abstract boolean isProxied();
  
  public abstract IResponse getWCCResponse();
  
  public abstract String getCipherSuite();
  
  public abstract X509Certificate[] getPeerCertificates();
  
  public abstract String getQueryString();
  
  public abstract Cookie[] getCookies();
  
  public abstract byte[] getCookieValue(String paramString);
  
  public abstract List getAllCookieValues(String paramString);
  
  public abstract boolean getShouldDestroy();
  
  public abstract void setShouldDestroy(boolean paramBoolean);
  
  public abstract void setShouldReuse(boolean paramBoolean);
  
  public abstract void setShouldClose(boolean paramBoolean);
  
  public abstract void removeHeader(String paramString);
  
  public abstract void startAsync();
  
//  public abstract ThreadPool getThreadPool();
  
  public abstract boolean isStartAsync();
  
  public abstract void lock();
  
  public abstract void unlock();
}
