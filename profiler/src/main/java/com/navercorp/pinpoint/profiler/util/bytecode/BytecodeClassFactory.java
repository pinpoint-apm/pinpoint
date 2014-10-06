package com.nhn.pinpoint.profiler.util.bytecode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import com.nhn.pinpoint.profiler.ProfilerException;

public class BytecodeClassFactory extends ClassVisitor implements Opcodes {
    private static final ConcurrentMap<ClassLoader, Map<String, BytecodeClass>> cache = new ConcurrentHashMap<ClassLoader, Map<String, BytecodeClass>>();
    
    public static BytecodeClass get(String internalName, ClassLoader loader) {
        BytecodeClass bytecodeClass = getFromCache(internalName, loader);
        
        if (bytecodeClass != null) {
            return bytecodeClass;
        }
        
        bytecodeClass = load(internalName, loader);
        addToCache(loader, bytecodeClass);
        
        return bytecodeClass;
    }
    
    private static BytecodeClass getFromCache(String internalName, ClassLoader loader) {
        Map<String, BytecodeClass> ofLoader = cache.get(loader);
        
        if (ofLoader == null) {
            return null;
        }
        
        return ofLoader.get(internalName);
    }
    
    private static void addToCache(ClassLoader loader, BytecodeClass bytecodeClass) {
        Map<String, BytecodeClass> ofLoader = cache.get(loader);
        
        if (ofLoader == null) {
            synchronized (cache) {
                ofLoader = cache.get(loader);
                
                if (ofLoader == null) {
                    ofLoader = new ConcurrentHashMap<String, BytecodeClass>();
                    cache.put(loader, ofLoader);
                }
            }
        }
        
        ofLoader.put(bytecodeClass.getName(), bytecodeClass);
    }

    private static BytecodeClass load(String internalName, ClassLoader loader) {
        InputStream is = loader.getResourceAsStream(internalName + ".class");
        
        if (is == null) {
            throw new ProfilerException("Cannot find bytecode of " + internalName + " from " + loader);
        }
        
        try {
            return new BytecodeClassFactory().get(new ClassReader(is), loader);
        } catch (IOException e) {
            throw new ProfilerException("Fail to load bytecode: " + internalName, e);
        }
    }
    
    public static BytecodeClass from(byte[] bytecode, ClassLoader loader) {
         // 일단 사용 용도가 같은 바이트코드를 여러 번 전달할 곳이 아니기 때문에 캐시에 넣을 필요가 없고,
         // 또, bytecode를 직접 받는 것이기 때문에 같은 이름에 다른 바이트코드가 올 수도 있기도 하니 이 메서드를 통하는 경우는 캐시에 넣지 않는다.
        return new BytecodeClassFactory().get(new ClassReader(bytecode), loader);
    }
    
    
    
    private int version;
    private int access;
    private String name;
    private String signature;
    private String superName;
    private String[] interfaces;
    private List<AnnotationAnalyzer> annotationAnalyzers;
    private final List<MethodAnalyzer> methodAnalyzers = new ArrayList<MethodAnalyzer>();
    
    private BytecodeClassFactory() {
        super(ASM5);
    }
    

    public BytecodeClass get(ClassReader reader, ClassLoader loader) {
        reader.accept(this, ClassReader.SKIP_CODE);
        
        List<BytecodeAnnotation> annotations = null;
        
        if (annotationAnalyzers != null) {
            annotations = new ArrayList<BytecodeAnnotation>();
            
            for (AnnotationAnalyzer analyzer : annotationAnalyzers) {
                annotations.add(analyzer.getValue());
            }
        }
        
        List<BytecodeMethod> methods = new ArrayList<BytecodeMethod>(methodAnalyzers.size());
        
        for (MethodAnalyzer analyzer : methodAnalyzers) {
            methods.add(analyzer.toASMMethod());
        }
        
        return new BytecodeClass(version, access, name, signature, superName, interfaces, annotations, methods, loader);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) {
            return null;
        }

        if (annotationAnalyzers == null) {
            annotationAnalyzers = new ArrayList<AnnotationAnalyzer>();
        }
        
        AnnotationAnalyzer analyzer = new AnnotationAnalyzer(desc);
        annotationAnalyzers.add(analyzer);
        
        return analyzer;
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodAnalyzer analyzer = new MethodAnalyzer(access, name, desc, signature, exceptions);
        methodAnalyzers.add(analyzer);
        
        return analyzer;
    }

    @Override
    public void visitSource(String source, String debug) {
        // do nothing
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        // do nothing
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // do nothing
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        // do nothing        
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    public void visitEnd() {
        // do nothing        
    }
    
    
    private static class NameValue {
        public String name;
        public Object value;
        
        public NameValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
    
    private static abstract class AbstractAnnotationAnalyzer<T> extends AnnotationVisitor {
        protected List<NameValue> elements;
        
        public AbstractAnnotationAnalyzer() {
            super(ASM5);
        }
        
        public void addElement(String name, Object value) {
           if (elements == null) {
               elements = new ArrayList<NameValue>();
           }
           
           elements.add(new NameValue(name, value));
        };

        @Override
        public void visit(String name, Object value) {
            addElement(name, value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            addElement(name, new BytecodeEnum(desc, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            AnnotationAnalyzer analyzer = new AnnotationAnalyzer(desc);
            addElement(name, analyzer);
            
            return analyzer;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            AnnotationArrayElementAnalyzer analyzer = new AnnotationArrayElementAnalyzer();
            addElement(name, analyzer);
            
            return analyzer;
        }

        @Override
        public void visitEnd() {
            if (elements != null) {
                for (NameValue entry : elements) {
                    if (entry.value instanceof AbstractAnnotationAnalyzer) {
                        AbstractAnnotationAnalyzer<?> analyzer = (AbstractAnnotationAnalyzer<?>)entry.value;
                        entry.value = analyzer.getValue();
                    }
                }
            }
        }
        
        public abstract T getValue();
    }
    
    private static class AnnotationAnalyzer extends AbstractAnnotationAnalyzer<BytecodeAnnotation> {
        private final String desc;        
        
        public AnnotationAnalyzer(String desc) {
            this.desc = desc;
        }
        
        public BytecodeAnnotation getValue() {
            Map<String, Object> map = new HashMap<String, Object>();
            
            if (elements != null) {
                for (NameValue entry : elements) {
                    map.put(entry.name, entry.value);
                }
            }
            
            return new BytecodeAnnotation(desc, map);
        }
    }
    
    private static class AnnotationArrayElementAnalyzer extends AbstractAnnotationAnalyzer<Object[]> {
        public Object[] getValue() {
            if (elements == null) {
                return new Object[0];
            }
            
            int size = elements.size();
            Object[] array = new Object[size];
            
            for (int i = 0; i < size; i++) {
                array[i] = elements.get(i).value;
            }
            
            return array;
        }
    }
    
    private static class MethodAnalyzer extends MethodVisitor {
        private final int access;
        private final String name;
        private final String desc;
        private final String signature;
        private final String[] exceptions;
        
        private List<AnnotationAnalyzer> annotationAnalyzers;
        
        public MethodAnalyzer(int access, String name, String desc, String signature, String[] exceptions) {
            super(ASM5);
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (!visible) {
                return null;
            }
            
            if (annotationAnalyzers == null) {
                annotationAnalyzers = new ArrayList<AnnotationAnalyzer>(); 
            }
            
            AnnotationAnalyzer analyzer = new AnnotationAnalyzer(desc);
            annotationAnalyzers.add(analyzer);
            
            return analyzer;
        }

        @Override
        public void visitParameter(String name, int access) {
            // do nothing
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return null;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            return null;
        }

        @Override
        public void visitAttribute(Attribute attr) {
            // do nothing
        }

        @Override
        public void visitEnd() {
            // do nothing
        }
        
        public BytecodeMethod toASMMethod() {
            List<BytecodeAnnotation> annotations = null;
            
            if (this.annotationAnalyzers != null) {
                annotations = new ArrayList<BytecodeAnnotation>();
                
                for (AnnotationAnalyzer analyzer : annotationAnalyzers) {
                    annotations.add(analyzer.getValue());
                }
            }
            
            return new BytecodeMethod(access, name, desc, signature, exceptions, annotations);
        }
    }
}
