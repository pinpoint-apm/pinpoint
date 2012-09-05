package com.profiler.interceptor.bci;

import java.io.IOException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.*;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.InterceptorRegistry;
import com.profiler.interceptor.LoggingInterceptor;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;

public class JavaAssistClass implements InstrumentClass {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private JavaAssistByteCodeInstrumentor instrumentor;
	private CtClass ctClass;

	public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass) {
		this.instrumentor = instrumentor;
		this.ctClass = ctClass;
	}

	public CtClass getCtClass() {
		return ctClass;
	}

	@Override
	public boolean insertCodeBeforeConstructor(String[] args, String code) {
		try {
			CtConstructor constructor = getConstructor(args);
			if (constructor == null) {
				return false;
			}
			constructor.insertBefore(code);
			return true;
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			return false;
		}
	}

	@Override
	public boolean insertCodeAfterConstructor(String[] args, String code) {
		try {
			CtConstructor constructor = getConstructor(args);
			if (constructor == null) {
				return false;
			}
			constructor.insertAfter(code);
			return true;
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			return false;
		}
	}

	@Override
	public boolean insertCodeBeforeMethod(String methodName, String[] args, String code) {
		try {
			CtMethod method = getMethod(methodName, args);
			if (method == null) {
				return false;
			}
			method.insertBefore(code);
			return true;
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			return false;
		}
	}

	@Override
	public boolean insertCodeAfterMethod(String methodName, String[] args, String code) {
		try {
			CtMethod method = getMethod(methodName, args);
			if (method == null) {
				return false;
			}
			method.insertAfter(code);
			return true;
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			return false;
		}
	}

    // TODO return type을 별도 exception으로 할지 추가 검토가 필요함.
    public boolean addTraceVariable(String variableName, String setterName, String getterName, String variableType) {
        try {
            CtClass type = instrumentor.getClassPool().get(variableType);
            CtField traceVariable = new CtField(type, variableName, ctClass);
            ctClass.addField(traceVariable);
            if (setterName != null) {
                CtMethod setterMethod = CtNewMethod.setter(setterName, traceVariable);
                ctClass.addMethod(setterMethod);
            }
            if (getterName != null) {
                CtMethod getterMethod = CtNewMethod.getter(getterName, traceVariable);
                ctClass.addMethod(getterMethod);
            }
            return true;
        } catch (NotFoundException e) {
            if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
        } catch (CannotCompileException e) {
            if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
        }
        return false;
    }

    public boolean addConstructorInterceptor(String[] args, Interceptor interceptor) {
        return addInterceptor(null, args, interceptor);
    }


    @Override
	public boolean addInterceptor(String methodName, String[] args, Interceptor interceptor) {
		return addInterceptor(methodName, args, interceptor, Type.auto);
	}

	@Override
	public boolean addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) {
		if (interceptor == null) {
			return false;
        }
        CtBehavior behavior = getBehavior(methodName, args);
        if (behavior == null) {
			return false;
		}

        return addInterceptor0(methodName, interceptor, type, behavior);
	}

    private CtBehavior getBehavior(String methodName, String[] args) {
        if (methodName == null) {
            return getConstructor(args);
        }
        return getMethod(methodName, args);
    }

    private boolean addInterceptor0(String methodName, Interceptor interceptor, Type type, CtBehavior behavior) {
        int id = InterceptorRegistry.addInterceptor(interceptor);
        try {
            if (type == Type.auto) {
                if (interceptor instanceof StaticAroundInterceptor) {
                    addStaticAroundInterceptor(methodName, id, behavior);
                } else if (interceptor instanceof StaticBeforeInterceptor) {
                    addStaticBeforeInterceptor(methodName, id, behavior);
                } else if (interceptor instanceof StaticAfterInterceptor) {
                    addStaticAfterInterceptor(methodName, id, behavior);
                } else {
                    return false;
                }
            } else if (type == Type.around && interceptor instanceof StaticAroundInterceptor) {
                addStaticAroundInterceptor(methodName, id, behavior);
            } else if (type == Type.before && interceptor instanceof StaticBeforeInterceptor) {
                addStaticBeforeInterceptor(methodName, id, behavior);
            } else if (type == Type.after && interceptor instanceof StaticAfterInterceptor) {
                addStaticAfterInterceptor(methodName, id, behavior);
            } else {
                return false;
            }
            return true;
        } catch (NotFoundException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } catch (CannotCompileException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return false;
    }

    private void addStaticAroundInterceptor(String methodName, int id, CtBehavior method) throws NotFoundException, CannotCompileException {
		addStaticBeforeInterceptor(methodName, id, method);
		addStaticAfterInterceptor(methodName, id, method);
	}

	private void addStaticAfterInterceptor(String methodName, int id, CtBehavior behavior) throws NotFoundException, CannotCompileException {
		StringBuilder after = new StringBuilder(1024);
		after.append("{");
        format(after, "  %1$s interceptor = (%1$s) com.profiler.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAfterInterceptor.class.getName(), id);
		String target = getTarget(behavior);
		String returnType = getReturnType(behavior);
        format(after, "  interceptor.after(%1$s, \"%2$s\", \"%3$s\", $args, %4$s);", target, ctClass.getName(), methodName, returnType);
		after.append("}");
		String buildAfter = after.toString();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("addStaticAfterInterceptor after behavior:" + behavior.getLongName() + " code:" + buildAfter);
		}
		behavior.insertAfter(buildAfter);


		StringBuilder catchCode = new StringBuilder(1024);
		catchCode.append("{");
        format(catchCode, "  %1$s interceptor = (%1$s) com.profiler.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAfterInterceptor.class.getName(), id);
        format(catchCode, "  interceptor.after(%1$s, \"%2$s\", \"%3$s\", $args, $e);", target, ctClass.getName(), methodName);
		catchCode.append("  throw $e;");
		catchCode.append("}");
		String buildCatch = catchCode.toString();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("addStaticAfterInterceptor catch behavior:" + behavior.getLongName() + " code:" + buildCatch);
		}
		CtClass th = instrumentor.getClassPool().get("java.lang.Throwable");
		behavior.addCatch(buildCatch, th);

	}

	private String getTarget(CtBehavior behavior) {
		boolean staticMethod = isStatic(behavior);
		if (staticMethod) {
			return "null";
		} else {
			return "this";
		}
	}

	public String getReturnType(CtBehavior behavior) throws NotFoundException {
		if (behavior instanceof CtMethod) {
			CtClass returnType = ((CtMethod) behavior).getReturnType();
			if (CtClass.voidType == returnType) {
				return "null";
			}
		}
		return "($w)$_";
	}

	private boolean isStatic(CtBehavior behavior) {
		int modifiers = behavior.getModifiers();
		return java.lang.reflect.Modifier.isStatic(modifiers);
	}

	private void addStaticBeforeInterceptor(String methodName, int id, CtBehavior behavior) throws CannotCompileException {
		StringBuilder code = new StringBuilder(1024);
		code.append("{");
        format(code, "  %1$s interceptor = (%1$s) com.profiler.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticBeforeInterceptor.class.getName(), id);
		String target = getTarget(behavior);
        format(code, "  interceptor.before(%1$s, \"%2$s\", \"%3$s\", $args);", target, ctClass.getName(), methodName);
		code.append("}");
		String buildBefore = code.toString();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("addStaticBeforeInterceptor catch behavior:" + behavior.getLongName() + " code:" + buildBefore);
		}

		if (behavior instanceof CtConstructor) {
			((CtConstructor) behavior).insertBeforeBody(buildBefore);
		} else {
			behavior.insertBefore(buildBefore);
		}
	}

    private void format(StringBuilder codeBlock, String format, Object... args) {
        Formatter formatter = new Formatter(codeBlock);
        formatter.format(format, args);
    }


	public boolean addDebugLogBeforeAfterMethod() {
		String className = this.ctClass.getName();
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
		int id = InterceptorRegistry.addInterceptor(loggingInterceptor);
		try {
			CtClass cc = this.instrumentor.getClassPool().get(className);
			CtMethod[] methods = cc.getDeclaredMethods();

			for (CtMethod method : methods) {
				if (method.isEmpty()) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine(method.getLongName() + " is empty.");
					}
					continue;
				}
				String methodName = method.getName();

				// TODO method의 prameter type을 interceptor에 별도 추가해야 될것으로 보임.
				String params = getParamsToString(method.getParameterTypes());
				addStaticAroundInterceptor(methodName, id, method);
			}
			return true;
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return false;
	}

	/**
	 * 제대로 동작안함 다시 봐야 될것 같음. 생성자일경우의 bytecode 수정시 에러가 남.
	 * 
	 * @return
	 */
	@Deprecated
	public boolean addDebugLogBeforeAfterConstructor() {
		String className = this.ctClass.getName();
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
		int id = InterceptorRegistry.addInterceptor(loggingInterceptor);
		try {
			CtClass cc = this.instrumentor.getClassPool().get(className);
			CtConstructor[] constructors = cc.getConstructors();

			for (CtConstructor constructor : constructors) {
				if (constructor.isEmpty()) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine(constructor.getLongName() + " is empty.");
					}
					continue;
				}
				String constructorName = constructor.getName();
				String params = getParamsToString(constructor.getParameterTypes());

				// constructor.insertAfter("{System.out.println(\"*****" +
				// constructorName + " Constructor:Param=(" + params +
				// ") is finished. \" + $args);}");
				// constructor.addCatch("{System.out.println(\"*****" +
				// constructorName + " Constructor:Param=(" + params +
				// ") is finished.\"); throw $e; }"
				// , instrumentor.getClassPool().get("java.lang.Throwable"));
				addStaticAroundInterceptor(constructorName, id, constructor);
			}
			return true;
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return false;
	}

	private String getParamsToString(CtClass[] params) throws NotFoundException {
		StringBuilder sb = new StringBuilder(512);
		if (params.length != 0) {
			int paramsLength = params.length;
			for (int loop = paramsLength - 1; loop > 0; loop--) {
				sb.append(params[loop].getName()).append(",");
			}
		}
		String paramsStr = sb.toString();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("params type:" + paramsStr);
		}
		return paramsStr;
	}

	private CtMethod getMethod(String methodName, String[] args) {
		try {
			CtClass[] params = getCtParameter(args);
			return ctClass.getDeclaredMethod(methodName, params);
		} catch (NotFoundException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return null;
	}

	private CtConstructor getConstructor(String[] args) {
		try {
			CtClass[] params = getCtParameter(args);
			return ctClass.getDeclaredConstructor(params);
		} catch (NotFoundException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return null;
	}

	private CtClass[] getCtParameter(String[] args) throws NotFoundException {
		if (args == null) {
			return null;
		}
		CtClass[] params = new CtClass[args.length];
		for (int i = 0; i < args.length; i++) {
			params[i] = instrumentor.getClassPool().getCtClass(args[i]);
		}
		return params;
	}

	@Override
	public byte[] toBytecode() {
		try {
			return ctClass.toBytecode();
		} catch (IOException e) {
			logger.log(Level.INFO, "IoException class:" + ctClass.getName() + " " + e.getMessage(), e);
		} catch (CannotCompileException e) {
			logger.log(Level.INFO, "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
		}
		return null;
	}

	public Class<?> toClass() {
		try {
			return ctClass.toClass();
		} catch (CannotCompileException e) {
			logger.log(Level.INFO, "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
		}
		return null;
	}
}
