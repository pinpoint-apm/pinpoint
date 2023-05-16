package jdk.internal.access;

import java.security.ProtectionDomain;

/**
 * Stub for compiler trick
 */
public interface JavaLangAccess {
    /**
     * Defines a class with the given name to a class loader.
     */
    Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source);

    /**
     * Defines a class with the given name to a class loader with
     * the given flags and class data.
     *
     * @see java.lang.invoke.MethodHandles.Lookup#defineClass
     */
    Class<?> defineClass(ClassLoader cl, Class<?> lookup, String name, byte[] b, ProtectionDomain pd, boolean initialize, int flags, Object classData);

    /**
     * Registers a shutdown hook.
     *
     * It is expected that this method with registerShutdownInProgress=true
     * is only used to register DeleteOnExitHook since the first file
     * may be added to the delete on exit list by the application shutdown
     * hooks.
     *
     * @param slot  the slot in the shutdown hook array, whose element
     *              will be invoked in order during shutdown
     * @param registerShutdownInProgress true to allow the hook
     *        to be registered even if the shutdown is in progress.
     * @param hook  the hook to be registered
     *
     * @throws IllegalStateException if shutdown is in progress and
     *         the slot is not valid to register.
     */
    void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook);
}
