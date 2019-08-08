/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.AnnotationInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.MatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.NotMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.OrMatcherOperator;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaehong.kim
 */
public class DefaultTransformerMatcher implements TransformerMatcher {
    private static final String OBJECT_CLASS_INTERNAL_NAME = "java/lang/Object";
    private static final String ANNOTATION_PACKAGE_INTERNAL_NAME = "java/lang/annotation";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HierarchyCaches interfaceCaches;
    private final HierarchyCaches annotationCaches;
    private final HierarchyCaches superCaches;

    public DefaultTransformerMatcher(final InstrumentMatcherCacheConfig cacheConfig) {
        this.interfaceCaches = newHierarchyCaches(cacheConfig.getInterfaceCacheSize(), cacheConfig.getInterfaceCacheEntrySize());
        this.annotationCaches = newHierarchyCaches(cacheConfig.getAnnotationCacheSize(), cacheConfig.getAnnotationCacheEntrySize());
        this.superCaches = newHierarchyCaches(cacheConfig.getSuperCacheSize(), cacheConfig.getSuperCacheEntrySize());
    }

    private HierarchyCaches newHierarchyCaches(final int size, final int entrySize) {
        if (size > 0) {
            return new DefaultHierarchyCaches(size, entrySize);
        }
        return new DisableHierarchyCaches();
    }

    public boolean match(ClassLoader classLoader, MatcherOperand operand, InternalClassMetadata classMetadata) {
        if (operand.isOperator()) {
            // operation
            return traversal(classLoader, operand, classMetadata);
        }

        if (operand instanceof ClassInternalNameMatcherOperand) {
            return matchClass((ClassInternalNameMatcherOperand) operand, classMetadata);
        } else if (operand instanceof PackageInternalNameMatcherOperand) {
            return matchPackage((PackageInternalNameMatcherOperand) operand, classMetadata);
        } else if (operand instanceof InterfaceInternalNameMatcherOperand) {
            return matchInterface(classLoader, (InterfaceInternalNameMatcherOperand) operand, classMetadata);
        } else if (operand instanceof AnnotationInternalNameMatcherOperand) {
            return matchAnnotation(classLoader, (AnnotationInternalNameMatcherOperand) operand, classMetadata);
        } else if (operand instanceof SuperClassInternalNameMatcherOperand) {
            return matchSuper(classLoader, (SuperClassInternalNameMatcherOperand) operand, classMetadata);
        } else {
            throw new IllegalArgumentException("unknown operand. operand=" + operand);
        }
    }

    boolean matchClass(final ClassInternalNameMatcherOperand operand, final InternalClassMetadata classMetadata) {
        if (classMetadata == null) {
            return false;
        }
        if (classMetadata.isInterface() || classMetadata.isAnnotation()) {
            // skip interface and annotation.
            return false;
        }
        return operand.match(classMetadata.getClassInternalName());
    }

    boolean matchPackage(final PackageInternalNameMatcherOperand operand, final InternalClassMetadata classMetadata) {
        if (classMetadata == null) {
            return false;
        }
        if (classMetadata.isInterface() || classMetadata.isAnnotation()) {
            // skip interface and annotation.
            return false;
        }
        return operand.match(classMetadata.getClassInternalName());
    }

    boolean matchAnnotation(final ClassLoader classLoader, final AnnotationInternalNameMatcherOperand operand, final InternalClassMetadata classMetadata) {
        if (classMetadata == null) {
            return false;
        }

        for (String annotationInternalName : classMetadata.getAnnotationInternalNames()) {
            if (operand.match(annotationInternalName)) {
                if (this.annotationCaches.isActive() && classMetadata.isAnnotation()) {
                    this.annotationCaches.put(operand.getAnnotationInternalName(), classMetadata.getClassInternalName());
                }
                return true;
            }
        }

        if (!operand.isConsiderMetaAnnotation()) {
            return false;
        }

        // consider meta annotation.
        for (String annotationInternalName : classMetadata.getAnnotationInternalNames()) {
            if (!annotationInternalName.startsWith(ANNOTATION_PACKAGE_INTERNAL_NAME)) {
                // skip java.lang.annotation
                if (this.annotationCaches.isActive() && this.annotationCaches.get(operand.getAnnotationInternalName(), annotationInternalName)) {
                    // find meta annotation hierarchy cache.
                    if (classMetadata.isAnnotation()) {
                        this.annotationCaches.put(operand.getAnnotationInternalName(), classMetadata.getClassInternalName());
                    }
                    return true;
                }

                if (matchAnnotation(classLoader, operand, readClassMetadata(classLoader, annotationInternalName))) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean matchInterface(final ClassLoader classLoader, final InterfaceInternalNameMatcherOperand operand, final InternalClassMetadata classMetadata) {
        if (classMetadata == null || classMetadata.getClassInternalName().equals(OBJECT_CLASS_INTERNAL_NAME)) {
            return false;
        }

        for (String interfaceInternalName : classMetadata.getInterfaceInternalNames()) {
            if (operand.match(interfaceInternalName)) {
                if (this.interfaceCaches.isActive() && !classMetadata.isSynthetic() && !classMetadata.isInnerClass()) {
                    // cache active AND NOT synthetic class AND NOT inner class.
                    this.interfaceCaches.put(operand.getInterfaceInternalName(), classMetadata.getClassInternalName());
                }
                return true;
            }
        }

        if (!operand.isConsiderHierarchy()) {
            return false;
        }

        // consider hierarchy.
        // interfaces.
        for (String interfaceInternalName : classMetadata.getInterfaceInternalNames()) {
            if (!operand.isJavaPackage() && interfaceInternalName.startsWith("java/")) {
                // skip java package.
                continue;
            }

            if (this.interfaceCaches.isActive() && this.interfaceCaches.get(operand.getInterfaceInternalName(), interfaceInternalName)) {
                // find interface hierarchy cache.
                if (!classMetadata.isSynthetic() && !classMetadata.isInnerClass()) {
                    // NOT synthetic class AND NOT inner class.
                    this.interfaceCaches.put(operand.getInterfaceInternalName(), classMetadata.getClassInternalName());
                }
                return true;
            }

            if (matchInterface(classLoader, operand, readClassMetadata(classLoader, interfaceInternalName))) {
                return true;
            }
        }

        // super
        if (this.interfaceCaches.isActive() && this.interfaceCaches.get(operand.getInterfaceInternalName(), classMetadata.getSuperClassInternalName())) {
            // find interface hierarchy cache.
            if (!classMetadata.isSynthetic() && !classMetadata.isInnerClass()) {
                // NOT synthetic class AND NOT inner class.
                this.interfaceCaches.put(operand.getInterfaceInternalName(), classMetadata.getClassInternalName());
            }
            return true;
        }

        return matchInterface(classLoader, operand, readClassMetadata(classLoader, classMetadata.getSuperClassInternalName()));
    }

    boolean matchSuper(final ClassLoader classLoader, final SuperClassInternalNameMatcherOperand operand, final InternalClassMetadata classMetadata) {
        if (classMetadata == null || classMetadata.getSuperClassInternalName() == null || classMetadata.getSuperClassInternalName().equals(OBJECT_CLASS_INTERNAL_NAME)) {
            return false;
        }

        if (operand.match(classMetadata.getSuperClassInternalName())) {
            if (this.superCaches.isActive() && !classMetadata.isSynthetic() && !classMetadata.isInnerClass()) {
                this.superCaches.put(operand.getSuperClassInternalNames(), classMetadata.getClassInternalName());
            }
            return true;
        }

        if (!operand.isConsiderHierarchy()) {
            return false;
        }

        if (classMetadata.getSuperClassInternalName() == null || classMetadata.getSuperClassInternalName().equals(OBJECT_CLASS_INTERNAL_NAME)) {
            return false;
        }

        if (!operand.isJavaPackage() && classMetadata.getSuperClassInternalName().startsWith("java/")) {
            // skip java package.
            return false;
        }

        if (this.superCaches.isActive() && this.superCaches.get(operand.getSuperClassInternalNames(), classMetadata.getSuperClassInternalName())) {
            if (!classMetadata.isSynthetic() && !classMetadata.isInnerClass()) {
                this.superCaches.put(operand.getSuperClassInternalNames(), classMetadata.getClassInternalName());
            }
            return true;
        }

        return matchSuper(classLoader, operand, readClassMetadata(classLoader, classMetadata.getSuperClassInternalName()));
    }

    private InternalClassMetadata readClassMetadata(final ClassLoader classLoader, final String classInternalName) {
        if (classInternalName == null) {
            // root.
            return null;
        }

        try {
            return InternalClassMetadataReader.readInternalClassMetadata(classLoader, classInternalName);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to read metadata of class. classLoader={}, internalName={}", classLoader, classInternalName, e);
            }
        }

        // not found.
        return null;
    }

    boolean traversal(ClassLoader classLoader, MatcherOperand operand, InternalClassMetadata classMetadata) {
        if (operand instanceof NotMatcherOperator) {
            NotMatcherOperator operator = (NotMatcherOperator) operand;
            if (operator.getRightOperand() == null) {
                throw new IllegalArgumentException("invalid operator - not found right operand. operator=" + operator);
            }

            final MatcherOperand rightOperand = operator.getRightOperand();
            // NOT
            return match(classLoader, rightOperand, classMetadata) == false;
        }

        MatcherOperator operator = (MatcherOperator) operand;
        // for binary operator.
        if (operator.getLeftOperand() == null) {
            throw new IllegalArgumentException("invalid operator - not found left operand. operator=" + operator);
        }
        final MatcherOperand leftOperand = operator.getLeftOperand();

        if (operator.getRightOperand() == null) {
            throw new IllegalArgumentException("invalid operator - not found right operand. operator=" + operator);
        }
        final MatcherOperand rightOperand = operator.getRightOperand();

        MatcherOperand firstOperand = leftOperand;
        MatcherOperand secondOperand = rightOperand;
        if (leftOperand.getExecutionCost() > rightOperand.getExecutionCost()) {
            // cost-based execution plan.
            firstOperand = rightOperand;
            secondOperand = leftOperand;
        }

        if (operand instanceof OrMatcherOperator) {
            // OR
            if (match(classLoader, firstOperand, classMetadata)) {
                return true;
            }
            return match(classLoader, secondOperand, classMetadata);
        } else if (operand instanceof AndMatcherOperator) {
            // AND
            if (match(classLoader, firstOperand, classMetadata)) {
                return match(classLoader, secondOperand, classMetadata);
            }
            return false;
        } else {
            throw new IllegalArgumentException("unknown operator. operator=" + operator);
        }
    }
}