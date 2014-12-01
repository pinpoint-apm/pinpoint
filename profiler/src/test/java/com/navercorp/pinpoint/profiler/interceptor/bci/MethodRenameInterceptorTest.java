package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.profiler.util.LoaderUtils;
import javassist.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author emeroad
 */
public class MethodRenameInterceptorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void methodRename() throws Exception {
        // Method rename을 사용해서 local 변수를 공유하는 방법의 경우 call stack이 변경되는 문제점이있다.
        String methodName = "callA";
        String objectName = "com.nhn.pinpoint.profiler.interceptor.bci.TestObject";
        // start by getting the class file and method

        final ClassPool classPool = new ClassPool(true);
        final Loader loader = getLoader(classPool);

        CtClass ctClazz = classPool.get(objectName);

        // add timing interceptor to the class
        addTiming(ctClazz, methodName);
        logger.debug("Added timing to method {}.{}", objectName, methodName);

        Class aClass = loader.loadClass(objectName);
        Object o = aClass.newInstance();
        Method method = o.getClass().getMethod(methodName);
        Object result = method.invoke(o);

    }

    /**
     * https://github.com/yangwm/JavaLearn/blob/master/reflect.src/jdyn/javassist/JassistTiming.java
     * @param clas
     * @param mname
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private void addTiming(CtClass clas, String mname)
            throws NotFoundException, CannotCompileException {

        //  get the method information (throws exception if method with
        //  given name is not declared directly by this class, returns
        //  arbitrary choice if more than one with the given name)
        CtMethod mold = clas.getDeclaredMethod(mname);

        //  rename old method to synthetic name, then duplicate the
        //  method with original name for use as interceptor
        String nname = mname + "$impl";
        mold.setName(nname);
        CtMethod mnew = CtNewMethod.copy(mold, mname, clas, null);

        //  start the body text generation by saving the start time
        //  to a local variable, then call the timed method; the
        //  actual code generated needs to depend on whether the
        //  timed method returns a value
        String type = mold.getReturnType().getName();
        StringBuilder body = new StringBuilder();
        body.append("{\nlong start = System.currentTimeMillis();\n");
        if (!"void".equals(type)) {
            body.append(type + " result = ");
        }
        body.append(nname + "($$);\n");

        //  finish body text generation with call to print the timing
        //  information, and return saved value (if not void)
        body.append("System.out.println(\"Call to method " + mname +
                " took \" +\n (System.currentTimeMillis()-start) + " +
                "\" ms.\");\n");
        if (!"void".equals(type)) {
            body.append("return result;\n");
        }
        body.append("}");

        //  replace the body of the interceptor method with generated
        //  code block and add it to class
        mnew.setBody(body.toString());
        clas.addMethod(mnew);
        //  print the generated code block just to show what was done
        logger.debug("Interceptor method body:");
        logger.debug("body:{}", body.toString());
    }

    private Loader getLoader(ClassPool pool) {
        return LoaderUtils.createLoader(pool);
    }

}
