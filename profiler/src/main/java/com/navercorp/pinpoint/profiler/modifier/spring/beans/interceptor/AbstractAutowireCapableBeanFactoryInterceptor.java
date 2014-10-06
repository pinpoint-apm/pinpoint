package com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.log4j.helpers.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.ClassFileRetransformer;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.ProfilerException;
import com.nhn.pinpoint.profiler.modifier.Modifier;

public class AbstractAutowireCapableBeanFactoryInterceptor implements SimpleAroundInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final ClassFileRetransformer retransformer;
    private final Modifier modifier;
    
    private final List<Pattern> targetNamePatterns;
    private final List<Pattern> targetClassPatterns;
    private final List<Class<? extends Annotation>> targetAnnotations;
    
    private final ConcurrentMap<Class<?>, Boolean> transformed = new ConcurrentHashMap<Class<?>, Boolean>();
    private final Cache<Class<?>, Boolean> rejectedCache = CacheBuilder.newBuilder().concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2).maximumSize(1024).weakKeys().build();

    public static AbstractAutowireCapableBeanFactoryInterceptor get(ProfilerConfig config, ClassFileRetransformer retransformer, Modifier modifier) {
        List<String> targetNamePatternStrings = split(config.getSpringBeansNamePatterns());
        List<String> targetClassPatternStrings = split(config.getSpringBeansClassPatterns());
        List<String> targetAnnotationNames = split(config.getSpringBeansAnnotations());

        List<Pattern> beanNamePatterns = null;
        
        if (!targetNamePatternStrings.isEmpty()) {
            beanNamePatterns = new ArrayList<Pattern>(targetNamePatternStrings.size());
            
            for (String s : targetNamePatternStrings) {
                Pattern p = Pattern.compile(s);
                beanNamePatterns.add(p);
            }
        }
        
        
        List<Pattern> beanClassPatterns = null;
        
        if (!targetClassPatternStrings.isEmpty()) {
            beanClassPatterns = new ArrayList<Pattern>(targetClassPatternStrings.size());
            
            for (String s : targetClassPatternStrings) {
                Pattern p = Pattern.compile(s);
                beanClassPatterns.add(p);
            }
        }
        
        
        List<Class<? extends Annotation>> beanAnnotationClasses = null;
        
        if (!targetAnnotationNames.isEmpty()) {
            beanAnnotationClasses = new ArrayList<Class<? extends Annotation>>(targetAnnotationNames.size());
            
            for (String s : targetAnnotationNames) {
                try {
                    Class<?> c = Loader.loadClass(s);
                    Class<? extends Annotation> ac = c.asSubclass(Annotation.class);
                    beanAnnotationClasses.add(ac);
                } catch (ClassNotFoundException e) {
                    throw new ProfilerException("Cannot load class: " + s, e);
                } catch (ClassCastException e) {
                    throw new ProfilerException("Given class is not subclass of Annotation: " + s, e);
                }
            }
        }
        
        return new AbstractAutowireCapableBeanFactoryInterceptor(retransformer, modifier, beanNamePatterns, beanClassPatterns, beanAnnotationClasses);
    }
    
    private static List<String> split(String values) {
        if (values == null) {
            return Collections.<String>emptyList();
        }
        
        String[] tokens = values.split(",");
        List<String> result = new ArrayList<String>(tokens.length); 

        for (String token : tokens) {
            String trimmed = token.trim();
            
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        
        return result;
    }
    
    private AbstractAutowireCapableBeanFactoryInterceptor(ClassFileRetransformer retransformer, Modifier modifier, List<Pattern> beanNamePatterns, List<Pattern> beanClassPatterns, List<Class<? extends Annotation>> beanAnnotations) {
        this.retransformer = retransformer;
        this.modifier = modifier;
        this.targetNamePatterns = beanNamePatterns;
        this.targetClassPatterns = beanClassPatterns;
        this.targetAnnotations = beanAnnotations;
    }

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (result == null) {
            return;
        }
        
        Class<? extends Object> clazz = result.getClass();
        String beanName = (String)args[0];
        
        if (transformed.containsKey(clazz)) {
            return;
        }
        
        if (rejectedCache.getIfPresent(clazz) == Boolean.TRUE) {
            return;
        }
        
        if (!isTarget(beanName, clazz)) {
            rejectedCache.put(clazz, Boolean.TRUE);
            return;
        }
        
        // TODO 상속받은 메서드도 추적하고 싶다면, parent class들도 찾아서 retransform해야 한다.
         // 우선은 자기 자신의 메서드들만 추적하도록 하고, 추후 요구사항이 생기면 구현한다.
        
        try {
            retransformer.retransform(clazz, modifier);

            if (logger.isInfoEnabled()) {
                logger.info("Retransform " + clazz.getName());
            }
        } catch (ProfilerException e) {
            logger.warn("Fail to retransform: " + clazz.getName(), e);
            return;
        }
        
        transformed.put(clazz, Boolean.TRUE);
    }

    private boolean isTarget(String beanName, Class<?> clazz) {
        if (targetAnnotations != null) {
            for (Class<? extends Annotation> a : targetAnnotations) {
                if (clazz.isAnnotationPresent(a)) {
                    return true;
                }
            }
            
            for (Annotation a : clazz.getAnnotations()) {
                for (Class<? extends Annotation> ac : targetAnnotations) {
                    if (a.annotationType().isAnnotationPresent(ac)) {
                        return true;
                    }
                } 
            }
        }
        
        if (targetClassPatterns != null) {
            String className = clazz.getName();
            
            for (Pattern pattern : targetClassPatterns) {
                if (pattern.matcher(className).matches()) {
                    return true;
                }
            }
        }

        if (targetNamePatterns != null) {
            for (Pattern pattern : targetNamePatterns) {
                if (pattern.matcher(beanName).matches()) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
