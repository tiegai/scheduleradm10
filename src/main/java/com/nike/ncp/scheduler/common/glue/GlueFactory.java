package com.nike.ncp.scheduler.common.glue;

import com.nike.ncp.scheduler.common.glue.impl.SpringGlueFactory;
import com.nike.ncp.scheduler.common.handler.IJobHandler;
import groovy.lang.GroovyClassLoader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * glue factory, product class/object by name
 */
public class GlueFactory {


    private static GlueFactory glueFact = new GlueFactory();

    public static GlueFactory getInstance() {
        return glueFact;
    }

    private static final int TYPE_0 = 0;
    private static final int TYPE_1 = 1;

    public static void refreshInstance(int type) {
        if (type == TYPE_0) {
            glueFact = new GlueFactory();
        } else if (type == TYPE_1) {
            glueFact = new SpringGlueFactory();
        }
    }


    /**
     * groovy class loader
     */
    private transient GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private transient ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();

    /**
     * load new instance, prototype
     *
     * @param codeSource
     * @return
     * @throws Exception
     */
    public IJobHandler loadNewInstance(String codeSource) throws Exception {
        if (codeSource != null && codeSource.trim().length() > 0) {
            Class<?> clazz = getCodeSourceClass(codeSource);
            if (clazz != null) {
                Object instance = clazz.newInstance();
                if (instance != null) {
                    if (instance instanceof IJobHandler) {
                        this.injectService(instance);
                        return (IJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, "
                                + "cannot convert from instance[" + instance.getClass() + "] to IJobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
    }

    private Class<?> getCodeSourceClass(String codeSource) {
        try {
            // md5
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes());
            String md5Str = new BigInteger(1, md5).toString(16);

            Class<?> clazz = classCache.get(md5Str);
            if (clazz == null) {
                clazz = groovyClassLoader.parseClass(codeSource);
                classCache.putIfAbsent(md5Str, clazz);
            }
            return clazz;
        } catch (Exception e) {
            return groovyClassLoader.parseClass(codeSource);
        }
    }

    /**
     * inject service of bean field
     *
     * @param instance
     */
    public void injectService(Object instance) {
        // do something
    }

}
