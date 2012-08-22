package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import javassist.ClassPool;
import javassist.NotFoundException;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaAssistByteCodeInstrumentor implements ByteCodeInstrumentor {

    private final Logger logger = Logger.getLogger(JavaAssistByteCodeInstrumentor.class.getName());

    private ClassPool classPool;

    public JavaAssistByteCodeInstrumentor() {
        this.classPool = createClassPool();
    }

    @Override
    public void addInterceptor(String className, String methodName, String[] args, Interceptor interceptor) {
        if(interceptor instanceof StaticBeforeInterceptor) {

        }
    }

    public ClassPool getClassPool() {
        return this.classPool;
    }

    private ClassPool createClassPool() {
        ClassPool classPool = new ClassPool(null);
        classPool.appendSystemPath();

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("CATALINA_HOME=" + catalinaHome);
            }

            appendClassPath(classPool, catalinaHome + "/lib/servlet-api.jar");
            appendClassPath(classPool, catalinaHome + "/lib/catalina.jar");
        }
        return classPool;
    }

    private void appendClassPath(ClassPool classPool, String pathName) {
        try {
            classPool.appendClassPath(pathName);
        } catch (NotFoundException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("lib not found. " + e.getMessage());
            }

        }
    }

    public void checkLibrary(ClassLoader classLoader, String javassistClassName) {
		// TODO Util로 뽑을까?
		boolean findClass = findClass(javassistClassName);
		if (findClass) {
			return;
		}
		loadClassLoaderLibraries(classLoader);
	}

	public boolean findClass(String javassistClassName) {
		// TODO 원래는 get인데. find는 ctclas를 생성하지 않아 변경. 어차피 아래서 생성하기는 함. 유효성 여부 확인
		// 필요
		URL url = classPool.find(javassistClassName);
		if (url == null) {
			return false;
		}
		return true;
	}

	private void loadClassLoaderLibraries(ClassLoader classLoader) {
		if (classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			// TODO classLoader가 가지고 있는 전체 리소스를 모두 로드해야 되는것인지? 테스트 케이스 만들어서
			// 확인해봐야 할듯.
			URL[] urlList = urlClassLoader.getURLs();
			for (URL tempURL : urlList) {
				String filePath = tempURL.getFile();
				try {
					classPool.appendClassPath(filePath);
					// TODO 여기서 로그로 class로더를 찍어보면 어떤 clasdLoader에서 로딩되는지 알수 있을거
					// 것같음.
					// 만약 한개만 로딩해도 된다면. return true 할것
					// log("Loaded "+filePath+" library.");

				} catch (NotFoundException e) {
				}
			}
		}
	}
}
