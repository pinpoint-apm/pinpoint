package com.ibm.websphere.servlet.response;

import com.ibm.websphere.servlet.request.IRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.Cookie;

public abstract interface IResponse
{
  public abstract void setStatusCode(int paramInt);
  
  public abstract OutputStream getOutputStream()
    throws IOException;
  
  public abstract boolean isCommitted();
  
  public abstract void addHeader(String paramString1, String paramString2);
  
  public abstract void addHeader(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
  
  public abstract void addDateHeader(String paramString, long paramLong);
  
  public abstract void addIntHeader(String paramString, int paramInt);
  
  public abstract void setDateHeader(String paramString, long paramLong);
  
  public abstract void setIntHeader(String paramString, int paramInt);
  
  public abstract Enumeration getHeaderNames();
  
  public abstract Enumeration getHeaders(String paramString);
  
  public abstract String getHeader(String paramString);
  
  public abstract Vector[] getHeaderTable();
  
  public abstract String getHeader(byte[] paramArrayOfByte);
  
  public abstract boolean containsHeader(String paramString);
  
  public abstract boolean containsHeader(byte[] paramArrayOfByte);
  
  public abstract void removeHeader(String paramString);
  
  public abstract void removeHeader(byte[] paramArrayOfByte);
  
  public abstract void clearHeaders();
  
  public abstract IRequest getWCCRequest();
  
  public abstract void setFlushMode(boolean paramBoolean);
  
  public abstract boolean getFlushMode();
  
  public abstract void flushBufferedContent();
  
  public abstract void setReason(String paramString);
  
  public abstract void setReason(byte[] paramArrayOfByte);
  
  public abstract void addCookie(Cookie paramCookie);
  
  public abstract Cookie[] getCookies();
  
  public abstract void prepareHeadersForWrite();
  
  public abstract void writeHeaders();
  
  public abstract void setHeader(String paramString1, String paramString2);
  
  public abstract void setHeader(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
  
  public abstract void setContentType(String paramString);
  
  public abstract void setContentType(byte[] paramArrayOfByte);
  
  public abstract void setContentLanguage(String paramString);
  
  public abstract void setContentLanguage(byte[] paramArrayOfByte);
  
  public abstract void setAllocateDirect(boolean paramBoolean);
  
  public abstract boolean isAllocateDirect();
  
  public abstract void setLastBuffer(boolean paramBoolean);
  
  public abstract void releaseChannel();
  
  public abstract void removeCookie(String paramString);
}
