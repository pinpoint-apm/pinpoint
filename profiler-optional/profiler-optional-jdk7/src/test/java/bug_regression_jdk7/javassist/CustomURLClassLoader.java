package bug_regression_jdk7.javassist;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CustomURLClassLoader extends URLClassLoader {

    public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public CustomURLClassLoader(URL[] urls) {
        super(urls);
    }

    public Class<?> defineClass0(String name, byte[] bytes) {
        return this.defineClass(name, bytes, 0, bytes.length);
    }

    public Class<?> defineClass0(String name, byte[] bytes, int off, int length) {
        return super.defineClass(name, bytes, off, length);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }
}

