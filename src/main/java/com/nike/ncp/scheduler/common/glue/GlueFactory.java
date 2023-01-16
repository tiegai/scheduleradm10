package com.nike.ncp.scheduler.common.glue;

import com.nike.ncp.scheduler.common.glue.impl.SpringGlueFactory;
import com.nike.ncp.scheduler.common.handler.IJobHandler;
import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * glue factory, product class/object by name
 */
public class GlueFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlueFactory.class);
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

    /**
     * groovy class loader
     */
/*    private transient GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private transient ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();*/
    @SuppressWarnings("all")
    private Class<?> getCodeSourceClass(String codeSource) {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();
        try {
            // md5
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes(Charset.forName("UTF-8")));
            String md5Str = new BigInteger(1, md5).toString(16);

            Class<?> clazz = classCache.get(md5Str);
            if (clazz == null) {
                clazz = groovyClassLoader.parseClass(codeSource);
                clazz = classCache.putIfAbsent(md5Str, clazz);
            }
            return clazz;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
