package com.nhn.pinpoint.profiler.interceptor.bci;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;


import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.profiler.util.Scope;
import com.nhn.pinpoint.profiler.util.ScopePool;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class JavaAssistByteCodeInstrumentor implements ByteCodeInstrumentor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();
    private final boolean isDebug = logger.isDebugEnabled();

    private final NamedClassPool rootClassPool;
    // classPool의 수평적 확장이 필요할수 있음. was에 여러개의 webapp가 있을 경우 충돌방지.
    private final NamedClassPool childClassPool;

    private Agent agent;

    private final ScopePool scopePool = new ScopePool();

    private final ClassLoadChecker classLoadChecker = new ClassLoadChecker();

    public JavaAssistByteCodeInstrumentor() {
        this.rootClassPool = createClassPool(null, "rootClassPool");
        this.childClassPool = new NamedClassPool(rootClassPool, "childClassPool");
    }

    public JavaAssistByteCodeInstrumentor(String[] pathNames, Agent agent) {
        this.rootClassPool = createClassPool(pathNames, "rootClassPool");
        this.childClassPool = createChildClassPool(rootClassPool, "childClassPool");
        this.agent = agent;
        // agent의 class는 rootClassPool에 넣는다.
        checkLibrary(this.getClass().getClassLoader(), this.rootClassPool, this.getClass().getName());
    }

    public Agent getAgent() {
        return agent;
    }

    public ClassPool getClassPool() {
        return this.childClassPool;
    }

    @Override
    public Scope getScope(String scopeName) {
        return this.scopePool.getScope(scopeName);
    }

    private NamedClassPool createClassPool(String[] pathNames, String classPoolName) {
        NamedClassPool classPool = new NamedClassPool(null, classPoolName);
        classPool.appendSystemPath();
        if (pathNames != null) {
            for (String path : pathNames) {
                appendClassPath(classPool, path);
            }
        }
        return classPool;
    }

    private NamedClassPool createChildClassPool(ClassPool rootClassPool, String classPoolName) {
        NamedClassPool childClassPool = new NamedClassPool(rootClassPool, classPoolName);
        childClassPool.appendSystemPath();
        childClassPool.childFirstLookup = true;
        return childClassPool;
    }


    private void appendClassPath(ClassPool classPool, String pathName) {
        try {
            classPool.appendClassPath(pathName);
        } catch (NotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("appendClassPath fail. lib not found. {}", e.getMessage(), e);
            }
        }
    }

    public void checkLibrary(ClassLoader classLoader, String javassistClassName) {
        checkLibrary(classLoader, this.childClassPool, javassistClassName);
    }

    public void checkLibrary(ClassLoader classLoader, NamedClassPool classPool, String javassistClassName) {
        // 최상위 classLoader일 경우 null이라 찾을필요가 없음.
        if (classLoader == null) {
            return;
        }
        // TODO Util로 뽑을까?
        boolean findClass = findClass(javassistClassName, classPool);
        if (findClass) {
            if (isDebug) {
                logger.debug("checkLibrary cl:{} clPool:{}, class:{} found.", classLoader, classPool.getName(), javassistClassName);
            }
            return;
        }
        loadClassLoaderLibraries(classLoader, classPool);
    }

    @Override
    public InstrumentClass getClass(String javassistClassName) throws InstrumentException {
        try {
            CtClass cc = childClassPool.get(javassistClassName);
            return new JavaAssistClass(this, cc);
        } catch (NotFoundException e) {
            throw new InstrumentException(javassistClassName + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException {
        if (isInfo) {
            logger.info("defineClass class:{}, cl:{}", defineClass, classLoader);
        }
        try {
            // classLoader로 락을 잡는게 안전함.
            // 어차피 classLoader에서 락을 잡고 들어오는점도 있고. 예외 사항이 발생할수 있기 때문에.
            // classLoader의 재진입 락을 잡고 들어오는게 무난함.
            synchronized (classLoader)  {
                if (this.classLoadChecker.exist(classLoader, defineClass)) {
                    return classLoader.loadClass(defineClass);
                } else {
                    final CtClass clazz = childClassPool.get(defineClass);

                    // 로그 레벨을 debug로 하니 개발때 제대로 체크 안하는 사람이 있어서 수정함.
                    checkTargetClassInterface(clazz);

                    defineAbstractSuperClass(clazz, classLoader, protectedDomain);
                    defineNestedClass(clazz, classLoader, protectedDomain);
                    return clazz.toClass(classLoader, protectedDomain);
                }
            }
        } catch (NotFoundException e) {
            throw new InstrumentException(defineClass + " class not found. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(defineClass + " class define fail. cl:" + classLoader + " Cause:" + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new InstrumentException(defineClass + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    private void checkTargetClassInterface(CtClass clazz) throws NotFoundException, InstrumentException {
        final String name = TargetClassLoader.class.getName();
        final CtClass[] interfaces = clazz.getInterfaces();
        for (CtClass anInterface : interfaces) {
            if (name.equals(anInterface.getName())) {
                return;
            }
        }
        throw new InstrumentException("newInterceptor() not support. " + clazz.getName());
    }

    private void defineAbstractSuperClass(CtClass clazz, ClassLoader classLoader, ProtectionDomain protectedDomain) throws NotFoundException, CannotCompileException {
        final CtClass superClass = clazz.getSuperclass();
        if (superClass == null) {
            // java.lang.Object가 아닌 경우 null은 안나올듯.
            return;
        }
        final int modifiers = superClass.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            if (this.classLoadChecker.exist(classLoader, superClass.getName())) {
                // nestedClass는 자기 자신에게만 속해 있으므로 로드 여부 체크가 필요 없으나 abstractClass는 같이 사용할수 있으므로 체크해야 된다.
                return;
            }
            if (isInfo) {
                logger.info("defineAbstractSuperClass class:{} cl:{}", superClass.getName(), classLoader);
            }
            // 좀더 정확하게 java 스펙처럼 하려면 제귀를 돌면서 추가로 super를 확인해야 되나. 구지 그래야 되나 싶다. 패스.
            // 스펙상 1차원 abstractClass만 지원하는 것으로..
            superClass.toClass(classLoader, protectedDomain);
        }
    }

    private void defineNestedClass(CtClass clazz, ClassLoader classLoader, ProtectionDomain protectedDomain) throws NotFoundException, CannotCompileException {
        CtClass[] nestedClasses = clazz.getNestedClasses();
        if (nestedClasses.length == 0) {
            return;
        }
        for (CtClass nested : nestedClasses) {
            // 재귀하면서 최하위부터 로드
            defineNestedClass(nested, classLoader, protectedDomain);
            if (isInfo) {
                logger.info("defineNestedClass class:{} cl:{}", nested.getName(), classLoader);
            }
            nested.toClass(classLoader, protectedDomain);
        }
    }

    public boolean findClass(String javassistClassName, ClassPool classPool) {
        URL url = classPool.find(javassistClassName);
        if (url == null) {
            return false;
        }
        return true;
    }

    @Override
    public Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException {
        Class<?> aClass = this.defineClass(classLoader, interceptorFQCN, protectedDomain);
        try {
            return (Interceptor) aClass.newInstance();
        } catch (InstantiationException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException {
        Class<?> aClass = this.defineClass(classLoader, interceptorFQCN, protectedDomain);
        try {
            Constructor<?> constructor = aClass.getConstructor(paramClazz);
            return (Interceptor) constructor.newInstance(params);
        } catch (InstantiationException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        }

    }

    private void loadClassLoaderLibraries(ClassLoader classLoader, NamedClassPool classPool) {
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            // classLoader가 가지고 있는 전체 리소스를 가능한 패스로 다 걸어야 됨
            // 임의의 class가 없을 경우 class의 byte code를 classpool에 적재 할 수 없음.
            URL[] urlList = urlClassLoader.getURLs();
            for (URL tempURL : urlList) {
                String filePath = tempURL.getFile();
                try {
                    classPool.appendClassPath(filePath);
                    // 만약 한개만 로딩해도 된다면. return true 할것
                    if (isInfo) {
                        logger.info("Loaded cl:{} classPool:{} {} ", classLoader.getClass().getName(), classPool.getName(), filePath);
                    }
                } catch (NotFoundException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("lib load fail. path:{} cl:{} clPool:{}, Cause:{}", filePath, classLoader, classPool.getName(), e.getMessage(), e);
                    }
                }
            }
        }
    }
}
