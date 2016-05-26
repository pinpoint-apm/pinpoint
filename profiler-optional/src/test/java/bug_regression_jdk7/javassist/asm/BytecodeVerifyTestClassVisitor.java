package bug_regression_jdk7.javassist.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BytecodeVerifyTestClassVisitor extends ClassVisitor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String name;

    public BytecodeVerifyTestClassVisitor(final ClassVisitor cv) {
        super(org.objectweb.asm.Opcodes.ASM5, cv);
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName,
                      final String[] interfaces) {
        this.name = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        logger.debug("mv:{}", mv);
        logger.debug("name:{}", name);
        logger.debug("desc:{}", desc);
        logger.debug("signature:{}", signature);
        if (name.contains("bytecodeVerifyError")) {
            return new AddIntVariableMethodAdapter(mv, ACC_PUBLIC, name, "()V");
        }
        return mv;
    }
}
