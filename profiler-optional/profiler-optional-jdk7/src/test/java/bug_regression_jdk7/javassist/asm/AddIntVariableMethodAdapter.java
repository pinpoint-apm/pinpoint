package bug_regression_jdk7.javassist.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AddIntVariableMethodAdapter extends AdviceAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String name;

    public AddIntVariableMethodAdapter(MethodVisitor mv, int acc, String name, String desc) {
        super(Opcodes.ASM5, mv, acc, name, desc);
        this.name = name;

    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }

    @Override
    protected void onMethodEnter() {
        logger.debug("onMethodEnter() add int local variable inst:ISTORE_7 {}", name);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, 7);
    }

}
