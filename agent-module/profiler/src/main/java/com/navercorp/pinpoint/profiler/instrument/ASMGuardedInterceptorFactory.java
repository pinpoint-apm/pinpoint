/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInjectedAsyncContextApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;
import com.navercorp.pinpoint.profiler.instrument.classloading.InterceptorDefineClassHelper;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates one exception-guard wrapper class per interceptor <b>class</b>, so the guarded
 * delegate call is a monomorphic {@code invokevirtual} on a concrete type instead of the shared
 * wrapper's single megamorphic {@code invokeinterface} site that every interceptor of a shape
 * funnels through. The generated class is functionally identical to the shared
 * {@code ExceptionHandle*} wrapper: every base-interface method delegates inside
 * {@code try/catch(Throwable)} and hands the throwable to the {@link ExceptionHandler}.
 * <p>
 * Generation is best-effort: any failure returns {@code null} and the caller falls back to the
 * shared wrapper, so the worst case is exactly today's behavior. Only interfaces whose methods
 * all return {@code void} are eligible (result-replacing shapes must not lose the return value).
 * <p>
 * The class is defined in the delegate's class loader (bootstrap-core types are visible from
 * everywhere; the concrete delegate type only from its own loader) under a pinpoint-owned name,
 * the same pattern {@code ASMInterceptorHolder} uses. One class per delegate class, cached via
 * {@link ClassValue} so unloading follows the delegate's loader.
 */
public final class ASMGuardedInterceptorFactory {
    // JUL on purpose: this can run before the plugin logging bridge is ready.
    private static final Logger logger = Logger.getLogger(ASMGuardedInterceptorFactory.class.getName());

    private static final String CLASS_NAME_PREFIX = "com.navercorp.pinpoint.profiler.instrument.interceptor.GuardedInterceptor$$";
    private static final String SCOPED_CLASS_NAME_PREFIX = "com.navercorp.pinpoint.profiler.instrument.interceptor.GuardedScopedInterceptor$$";
    private static final AtomicInteger CLASS_ID = new AtomicInteger();

    /**
     * Scoped wrappers carry real logic (scope enter/leave, execution policy) beyond the guarded
     * delegation, so instead of emitting them from scratch they are produced by rewriting the
     * compiled bytes of the shared wrapper — the semantics stay maintained in one Java source.
     * One template per void-only shape; the non-void shapes (Block, ResultReplace) keep the
     * shared wrappers.
     */
    private static final Map<Class<?>, Class<?>> SCOPED_TEMPLATES = buildScopedTemplates();

    private static Map<Class<?>, Class<?>> buildScopedTemplates() {
        final Map<Class<?>, Class<?>> templates = new HashMap<>();
        templates.put(AroundInterceptor.class, ExceptionHandleScopedInterceptor.class);
        templates.put(AroundInterceptor0.class, ExceptionHandleScopedInterceptor0.class);
        templates.put(AroundInterceptor1.class, ExceptionHandleScopedInterceptor1.class);
        templates.put(AroundInterceptor2.class, ExceptionHandleScopedInterceptor2.class);
        templates.put(AroundInterceptor3.class, ExceptionHandleScopedInterceptor3.class);
        templates.put(AroundInterceptor4.class, ExceptionHandleScopedInterceptor4.class);
        templates.put(AroundInterceptor5.class, ExceptionHandleScopedInterceptor5.class);
        templates.put(StaticAroundInterceptor.class, ExceptionHandleScopedStaticAroundInterceptor.class);
        templates.put(ApiIdAwareAroundInterceptor.class, ExceptionHandleScopedApiIdAwareAroundInterceptor.class);
        templates.put(InjectedAsyncContextApiIdAwareAroundInterceptor.class, ExceptionHandleScopedInjectedAsyncContextApiIdAwareAroundInterceptor.class);
        return templates;
    }

    /**
     * The templates live in bootstrap-core, which the packaged agent appends to the bootstrap
     * class loader search — appended jars serve classes but not resources, so
     * {@code Class.getResourceAsStream} returns null there and the template bytes must be read
     * straight from the bootstrap jar list instead. Absent (unit tests, plain-classpath runs)
     * the resource lookup alone suffices.
     */
    private static volatile BootstrapCore templateSource;
    private static final ConcurrentMap<Class<?>, byte[]> TEMPLATE_BYTES_CACHE = new ConcurrentHashMap<>();

    public static void initTemplateSource(BootstrapCore bootstrapCore) {
        if (bootstrapCore != null && templateSource == null) {
            templateSource = bootstrapCore;
        }
    }

    private static final String DELEGATE_FIELD = "delegate";
    private static final String HANDLER_FIELD = "exceptionHandler";
    private static final Type HANDLER_TYPE = Type.getType(ExceptionHandler.class);
    private static final String HANDLE_EXCEPTION_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Throwable.class));

    /**
     * Wrapper classes keyed by delegate class. {@code ClassValue} keeps the association on the
     * delegate class itself, so a discarded class loader takes its generated wrappers with it.
     * A failed generation is cached as {@code null} holder content and never retried — the
     * fallback path is permanent for that class.
     */
    private static final ClassValue<GeneratedWrapper> WRAPPER_CLASSES = new ClassValue<GeneratedWrapper>() {
        @Override
        protected GeneratedWrapper computeValue(Class<?> delegateClass) {
            return generate(delegateClass);
        }
    };

    private static final ClassValue<GeneratedWrapper> SCOPED_WRAPPER_CLASSES = new ClassValue<GeneratedWrapper>() {
        @Override
        protected GeneratedWrapper computeValue(Class<?> delegateClass) {
            return generateScoped(delegateClass);
        }
    };

    private ASMGuardedInterceptorFactory() {
    }

    /**
     * Returns the delegate wrapped in its per-class generated guard, or {@code null} when the
     * shape is ineligible or generation failed — the caller must then use the shared wrapper.
     * The generated class implements the delegate's single interceptor interface, so the woven
     * CHECKCAST to the shape's base interface holds and the shared-wrapper {@code instanceof}
     * cascade would have picked the same shape.
     */
    public static Interceptor wrap(Interceptor delegate, ExceptionHandler exceptionHandler) {
        final GeneratedWrapper wrapper = WRAPPER_CLASSES.get(delegate.getClass());
        if (wrapper == null) {
            return null;
        }
        try {
            return (Interceptor) wrapper.constructor.newInstance(delegate, exceptionHandler);
        } catch (Throwable th) {
            logger.log(Level.WARNING, "Guarded interceptor instantiation failed, falling back to the shared wrapper. delegate=" + delegate.getClass().getName(), th);
            return null;
        }
    }

    /**
     * Scoped variant: returns the delegate wrapped in a per-class rewrite of the shared scoped
     * guard template, or {@code null} when ineligible or generation failed — the caller falls
     * back to the shared scoped wrapper. Same monomorphic-delegate goal as {@link #wrap}, but the
     * class is produced by retyping the template's delegate field instead of emitting from
     * scratch, so the scope enter/leave semantics stay in the template's Java source.
     */
    public static Interceptor wrapScoped(Interceptor delegate, InterceptorScope scope, ExecutionPolicy policy, ExceptionHandler exceptionHandler) {
        final GeneratedWrapper wrapper = SCOPED_WRAPPER_CLASSES.get(delegate.getClass());
        if (wrapper == null) {
            return null;
        }
        try {
            return (Interceptor) wrapper.constructor.newInstance(delegate, scope, policy, exceptionHandler);
        } catch (Throwable th) {
            logger.log(Level.WARNING, "Scoped guarded interceptor instantiation failed, falling back to the shared wrapper. delegate=" + delegate.getClass().getName(), th);
            return null;
        }
    }

    private static GeneratedWrapper generate(Class<?> delegateClass) {
        try {
            if (!Modifier.isPublic(delegateClass.getModifiers())) {
                // the generated class lives in a pinpoint-owned package; a non-public delegate
                // would fail the invokevirtual access check at first execution.
                return null;
            }
            final Class<?> baseInterface = findEligibleBaseInterface(delegateClass);
            if (baseInterface == null) {
                return null;
            }
            final String className = CLASS_NAME_PREFIX + CLASS_ID.getAndIncrement();
            final byte[] bytes = emit(className, delegateClass, baseInterface);
            final Class<?> wrapperClass = InterceptorDefineClassHelper.defineClass(delegateClass.getClassLoader(), className, bytes);
            final Constructor<?> constructor = wrapperClass.getConstructor(delegateClass, ExceptionHandler.class);
            return new GeneratedWrapper(constructor);
        } catch (Throwable th) {
            logger.log(Level.WARNING, "Guarded interceptor generation failed, falling back to the shared wrapper. delegate=" + delegateClass.getName(), th);
            return null;
        }
    }

    private static GeneratedWrapper generateScoped(Class<?> delegateClass) {
        try {
            if (!Modifier.isPublic(delegateClass.getModifiers())) {
                return null;
            }
            final Class<?> baseInterface = findEligibleBaseInterface(delegateClass);
            if (baseInterface == null) {
                return null;
            }
            final Class<?> template = SCOPED_TEMPLATES.get(baseInterface);
            if (template == null) {
                return null;
            }
            final String className = SCOPED_CLASS_NAME_PREFIX + CLASS_ID.getAndIncrement();
            final byte[] bytes = rewriteTemplate(template, className, baseInterface, delegateClass);
            final Class<?> wrapperClass = InterceptorDefineClassHelper.defineClass(delegateClass.getClassLoader(), className, bytes);
            final Constructor<?> constructor = wrapperClass.getConstructor(delegateClass, InterceptorScope.class, ExecutionPolicy.class, ExceptionHandler.class);
            return new GeneratedWrapper(constructor);
        } catch (Throwable th) {
            logger.log(Level.WARNING, "Scoped guarded interceptor generation failed, falling back to the shared wrapper. delegate=" + delegateClass.getName(), th);
            return null;
        }
    }

    /**
     * Rewrites the compiled template: the class is renamed, every reference to the base interface
     * — the delegate field's type, the constructor parameter, the {@code checkcast} javac emits
     * for {@code Objects.requireNonNull}, the stack map frame entries — is retyped to the
     * concrete delegate class, and calls dispatched on it switch from {@code invokeinterface} to
     * a monomorphic {@code invokevirtual}. The {@code implements} clause alone keeps the base
     * interface, so the woven CHECKCAST to the shape's type still holds.
     */
    private static byte[] rewriteTemplate(Class<?> template, String newClassName, Class<?> baseInterface, Class<?> delegateClass) throws IOException {
        byte[] templateBytes = TEMPLATE_BYTES_CACHE.get(template);
        if (templateBytes == null) {
            templateBytes = readTemplateBytes(template);
            TEMPLATE_BYTES_CACHE.putIfAbsent(template, templateBytes);
        }
        final String newInternalName = JavaAssistUtils.javaNameToJvmName(newClassName);
        final String baseInterfaceInternalName = Type.getInternalName(baseInterface);
        final String concreteInternalName = Type.getInternalName(delegateClass);

        final Map<String, String> mapping = new HashMap<>();
        mapping.put(Type.getInternalName(template), newInternalName);
        mapping.put(baseInterfaceInternalName, concreteInternalName);

        final ClassReader classReader = new ClassReader(templateBytes);
        // COMPUTE_MAXS: instructions are unchanged, but descriptors change length. EXPAND_FRAMES
        // because the remapper requires expanded frames to retype their entries.
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassRemapper remapper = new RetypingClassRemapper(classWriter, new SimpleRemapper(mapping), concreteInternalName);
        classReader.accept(remapper, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private static byte[] readTemplateBytes(Class<?> template) throws IOException {
        final InputStream inputStream = openTemplateStream(template);
        if (inputStream == null) {
            throw new IOException("not found template class resource " + template.getName());
        }
        try {
            return IOUtils.toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
    }

    private static InputStream openTemplateStream(Class<?> template) {
        final InputStream inputStream = template.getResourceAsStream(template.getSimpleName() + ".class");
        if (inputStream != null) {
            return inputStream;
        }
        final BootstrapCore bootstrapCore = templateSource;
        if (bootstrapCore != null) {
            final String resourceName = JavaAssistUtils.javaNameToJvmName(template.getName()) + ".class";
            return bootstrapCore.openStream(resourceName);
        }
        return null;
    }

    /**
     * {@link ClassRemapper} variant with the two deviations the template rewrite needs: the
     * {@code implements} clause is NOT remapped (the wrapper must stay castable to the shape's
     * base interface at the woven call site), and any {@code invokeinterface} whose owner was
     * retyped to the concrete delegate class becomes {@code invokevirtual} (an interface call on
     * a class owner would not link).
     */
    private static final class RetypingClassRemapper extends ClassRemapper {
        private final String concreteInternalName;

        private RetypingClassRemapper(ClassWriter classWriter, SimpleRemapper remapper, String concreteInternalName) {
            super(classWriter, remapper);
            this.concreteInternalName = concreteInternalName;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            // rename the class but keep the implements clause verbatim.
            cv.visit(version, access, remapper.mapType(name), signature, superName, interfaces);
        }

        @Override
        protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
            return super.createMethodRemapper(new InterfaceCallFixer(methodVisitor, concreteInternalName));
        }
    }

    private static final class InterfaceCallFixer extends MethodVisitor {
        private final String concreteInternalName;

        private InterfaceCallFixer(MethodVisitor methodVisitor, String concreteInternalName) {
            super(ASMVersion.VERSION, methodVisitor);
            this.concreteInternalName = concreteInternalName;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKEINTERFACE && concreteInternalName.equals(owner)) {
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false);
                return;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    /**
     * The single interceptor interface the wrapper should implement. Deliberately conservative:
     * the delegate must expose exactly one direct interceptor interface (walking up the class
     * hierarchy), and every method of it must return {@code void}. Anything else is the shared
     * wrapper's job.
     */
    private static Class<?> findEligibleBaseInterface(Class<?> delegateClass) {
        Class<?> found = null;
        for (Class<?> c = delegateClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Class<?> itf : c.getInterfaces()) {
                if (Interceptor.class.isAssignableFrom(itf) && itf != Interceptor.class) {
                    if (found != null && found != itf) {
                        return null;
                    }
                    found = itf;
                }
            }
        }
        if (found == null) {
            return null;
        }
        for (Method method : found.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isDefault()) {
                continue;
            }
            if (method.getReturnType() != void.class) {
                return null;
            }
        }
        return found;
    }

    private static byte[] emit(String className, Class<?> delegateClass, Class<?> baseInterface) {
        final String internalName = JavaAssistUtils.javaNameToJvmName(className);
        final Type delegateType = Type.getType(delegateClass);

        // COMPUTE_MAXS only: every frame in the emitted code is trivial (single-block try/catch
        // per method), and COMPUTE_FRAMES would resolve types through the wrong class loader.
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                internalName, null, "java/lang/Object", new String[]{Type.getInternalName(baseInterface)});

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, DELEGATE_FIELD, delegateType.getDescriptor(), null, null).visitEnd();
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, HANDLER_FIELD, HANDLER_TYPE.getDescriptor(), null, null).visitEnd();

        emitConstructor(cw, internalName, delegateType);
        for (Method method : baseInterface.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isDefault()) {
                continue;
            }
            emitDelegatingMethod(cw, internalName, delegateType, method);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void emitConstructor(ClassWriter cw, String internalName, Type delegateType) {
        final String ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, delegateType, HANDLER_TYPE);
        final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, DELEGATE_FIELD, delegateType.getDescriptor());
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, HANDLER_FIELD, HANDLER_TYPE.getDescriptor());
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * <pre>
     * public void m(...) {
     *     try {
     *         this.delegate.m(...);   // invokevirtual on the concrete type
     *     } catch (Throwable t) {
     *         this.exceptionHandler.handleException(t);
     *     }
     * }
     * </pre>
     */
    private static void emitDelegatingMethod(ClassWriter cw, String internalName, Type delegateType, Method method) {
        final String methodDesc = Type.getMethodDescriptor(method);
        final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), methodDesc, null, null);
        mv.visitCode();

        final Label start = new Label();
        final Label end = new Label();
        final Label handler = new Label();
        mv.visitTryCatchBlock(start, end, handler, "java/lang/Throwable");

        mv.visitLabel(start);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, internalName, DELEGATE_FIELD, delegateType.getDescriptor());
        int slot = 1;
        for (Type argumentType : Type.getArgumentTypes(methodDesc)) {
            mv.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), slot);
            slot += argumentType.getSize();
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, delegateType.getInternalName(), method.getName(), methodDesc, false);
        mv.visitLabel(end);
        final Label ret = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, ret);

        mv.visitLabel(handler);
        // frame: stack=[Throwable], locals=this+args. COMPUTE_MAXS needs the frame spelled out
        // only on class-file version >= V1_7, which V1_8 is.
        final Object[] locals = buildLocalsFrame(internalName, methodDesc);
        mv.visitFrame(Opcodes.F_NEW, locals.length, locals, 1, new Object[]{"java/lang/Throwable"});
        final int throwableSlot = slot;
        mv.visitVarInsn(Opcodes.ASTORE, throwableSlot);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, internalName, HANDLER_FIELD, HANDLER_TYPE.getDescriptor());
        mv.visitVarInsn(Opcodes.ALOAD, throwableSlot);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, HANDLER_TYPE.getInternalName(), "handleException", HANDLE_EXCEPTION_DESC, true);

        mv.visitLabel(ret);
        mv.visitFrame(Opcodes.F_NEW, locals.length, locals, 0, new Object[0]);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static Object[] buildLocalsFrame(String internalName, String methodDesc) {
        final Type[] argumentTypes = Type.getArgumentTypes(methodDesc);
        final Object[] locals = new Object[1 + argumentTypes.length];
        locals[0] = internalName;
        for (int i = 0; i < argumentTypes.length; i++) {
            locals[1 + i] = toFrameType(argumentTypes[i]);
        }
        return locals;
    }

    private static Object toFrameType(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return Opcodes.INTEGER;
            case Type.FLOAT:
                return Opcodes.FLOAT;
            case Type.LONG:
                return Opcodes.LONG;
            case Type.DOUBLE:
                return Opcodes.DOUBLE;
            default:
                return type.getInternalName();
        }
    }

    private static final class GeneratedWrapper {
        private final Constructor<?> constructor;

        private GeneratedWrapper(Constructor<?> constructor) {
            this.constructor = constructor;
        }
    }
}
