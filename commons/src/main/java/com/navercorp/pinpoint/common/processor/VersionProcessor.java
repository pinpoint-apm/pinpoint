package com.navercorp.pinpoint.common.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class VersionProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();
        try {
            JavaFileObject serviceRegistry = filer.createSourceFile("ServiceRegistry");
            Writer writer = serviceRegistry.openWriter();
            writer.write("public class ServiceRegistry {");
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Version Processor");
        }
        return true;
    }
}
