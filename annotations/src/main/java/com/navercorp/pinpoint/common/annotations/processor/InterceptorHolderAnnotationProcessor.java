package com.navercorp.pinpoint.common.annotations.processor;

import com.navercorp.pinpoint.common.annotations.InterceptorHolderBootstrap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes("com.navercorp.pinpoint.common.annotations.InterceptorHolderBootstrap")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InterceptorHolderAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        int count = 0;
        for (TypeElement annotation : annotations) {
            Set<? extends javax.lang.model.element.Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (javax.lang.model.element.Element element : elements) {
                InterceptorHolderBootstrap interceptorHolderBootstrap = element.getAnnotation(InterceptorHolderBootstrap.class);
                if (interceptorHolderBootstrap != null) {
                    int value = interceptorHolderBootstrap.value();
                    count = value;
                    break;
                }
            }
        }
        if (count <= 0) {
            System.out.println("Not found @InterceptorHolderBootstrap or value is less than 1");
            return true;
        }

        Filer filer = processingEnv.getFiler();
        for (int i = 0; i < count; i++) {
            final String className = "InterceptorHolder$$" + i;
            try {
                JavaFileObject fileObject = filer.createSourceFile("com.navercorp.pinpoint.bootstrap.interceptor.holder." + className);
                try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {
                    String tab = "  ";

                    // package com.pbear.devtool;
                    out.println("package com.navercorp.pinpoint.bootstrap.interceptor.holder;");
                    out.println("import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;");
                    out.println("import java.util.function.Supplier;");
                    out.print("public class ");
                    out.print(className);
                    out.println(" {");
                    out.println(tab + "private static Supplier<Interceptor> supplier;");
                    out.println(tab + "public static Interceptor get() {");
                    out.println(tab + tab + "return LazyLoading.INTERCEPTOR;");
                    out.println(tab + "}");
                    out.println(tab + "public static void set(Supplier interceptorSupplier) {");
                    out.println(tab + tab + "supplier = interceptorSupplier;");
                    out.println(tab + "}");
                    out.println(tab + "static class LazyLoading {");
                    out.println(tab + tab + "static Interceptor INTERCEPTOR = supplier.get();");
                    out.println(tab + "}");
                    out.println("}");
                }
            } catch (IOException e) {
                System.out.println("Failed to write InterceptorHolder$$" + i);
                e.printStackTrace();
            }
        }
        return true;
    }
}
