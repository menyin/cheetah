package com.caisheng.cheetah.api.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SpiLoader {
    private static Map<String, Object> CACHE = new ConcurrentHashMap<>();



    public static <T> T load(Class<T> claszz) {
        return load(claszz, null);
    }

    public static <T> T load(Class<T> claszz, String name) {
        String key = claszz.getName();
        Object obj = CACHE.get(key);
        if (obj == null) {
            T t = load0(claszz, name);
            if (t != null) {
                CACHE.put(key, t);
                return t;
            }
        } else if(claszz.isInstance(obj)){
            return (T) obj;
        }
        return load0(claszz,name);
    }

    private static <T> T load0(Class<T> claszz, String name) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(claszz);
        T t = filterByName(serviceLoader, name);

        if (t != null) {
            return t;
        } else {
            throw new IllegalStateException("Can not find META-INF/services/" + claszz.getName() + "on classpath.");
        }
    }

    /**
     *
     * @param serviceLoader
     * @param name
     * @param <T>
     * @return
     */
    private static <T> T filterByName(ServiceLoader<T> serviceLoader, String name) {
        Iterator<T> iterator = serviceLoader.iterator();
        if (name == null) {
            ArrayList<T> list = new ArrayList<>();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
            if (list.size() > 1) {
                list.sort((t1,t2)->{
                    Spi spi1 = t1.getClass().getAnnotation(Spi.class);
                    Spi spi2 = t2.getClass().getAnnotation(Spi.class);
                    int sort1 = spi1 == null ? 0 : spi1.sort();
                    int sort2 = spi2 == null ? 0 : spi2.sort();
                    return sort1 - sort2;
                });

            }
            if (list.size() > 0) {
                return list.get(0);
            }
        }else{
            while (iterator.hasNext()) {
                T t = iterator.next();
                if (t.getClass().getName().equals(name) || t.getClass().getSimpleName().equals(name)) {
                    return t;
                }
            }
        }
        return null;
    }
}
