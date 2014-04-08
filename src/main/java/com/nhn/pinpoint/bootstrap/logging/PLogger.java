package com.nhn.pinpoint.bootstrap.logging;

/**
 * @author emeroad
 */
public interface PLogger {


    void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args);

    void beforeInterceptor(Object target, Object[] args);

    void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result);

    void afterInterceptor(Object target, Object[] args, Object result);

    void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args);

    void afterInterceptor(Object target, Object[] args);

    boolean isTraceEnabled();


    void trace(String msg);


    void trace(String format, Object arg);



    void trace(String format, Object arg1, Object arg2);

    void trace(String format, Object[] argArray);

    void trace(String msg, Throwable t);



    public boolean isDebugEnabled();



    public void debug(String msg);



    public void debug(String format, Object arg);




    public void debug(String format, Object arg1, Object arg2);


    public void debug(String format, Object[] argArray);


    public void debug(String msg, Throwable t);





    public boolean isInfoEnabled();


    public void info(String msg);



    public void info(String format, Object arg);



    public void info(String format, Object arg1, Object arg2);


    public void info(String format, Object[] argArray);


    public void info(String msg, Throwable t);



    public boolean isWarnEnabled();


    public void warn(String msg);


    public void warn(String format, Object arg);


    public void warn(String format, Object[] argArray);


    public void warn(String format, Object arg1, Object arg2);


    public void warn(String msg, Throwable t);



    public boolean isErrorEnabled();


    public void error(String msg);


    public void error(String format, Object arg);


    public void error(String format, Object arg1, Object arg2);


    public void error(String format, Object[] argArray);


    public void error(String msg, Throwable t);




}
