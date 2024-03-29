package com.caisheng.cheetah.tools.config;

import com.typesafe.config.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal implementation detail, not ABI stable, do not touch.
 * For use only by the {@link com.typesafe.config} package.
 */
public final class ConfigBeanImpl{

    /**
     * This is public ONLY for use by the "config" package, DO NOT USE this ABI
     * may change.
     *
     * @param <T>    type of the bean
     * @param config config to use
     * @param clazz  class of the bean
     * @return the bean instance
     */
    public static <T> T createInternal(Config config, Class<T> clazz) {

        Map<String, ConfigValue> configProps = new HashMap<String, ConfigValue>();
        Map<String, String> originalNames = new HashMap<String, String>();
        for (Map.Entry<String, ConfigValue> configProp : config.root().entrySet()) {
            String originalName = configProp.getKey();
            String camelName = toCamelCase(originalName);
            // if a setting is in there both as some hyphen name and the camel name,
            // the camel one wins
            if (originalNames.containsKey(camelName) && !originalName.equals(camelName)) {
                // if we aren't a camel name to start with, we lose.
                // if we are or we are the first matching key, we win.
            } else {
                configProps.put(camelName, (ConfigValue) configProp.getValue());
                originalNames.put(camelName, originalName);
            }
        }

        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new ConfigException.BadBean("Could not get bean information for class " + clazz.getName(), e);
        }

        try {
            List<PropertyDescriptor> beanProps = new ArrayList<PropertyDescriptor>();
            for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
                if (beanProp.getReadMethod() == null || beanProp.getWriteMethod() == null) {
                    continue;
                }
                beanProps.add(beanProp);
            }

            // Fill in the bean instance
            T bean = clazz.newInstance();
            for (PropertyDescriptor beanProp : beanProps) {
                Method setter = beanProp.getWriteMethod();
                Type parameterType = setter.getGenericParameterTypes()[0];
                Class<?> parameterClass = setter.getParameterTypes()[0];
                String configKey = originalNames.get(beanProp.getName());
                if (configKey != null) {
                    Object unwrapped = getValue(clazz, parameterType, parameterClass, config, configKey);
                    setter.invoke(bean, unwrapped);
                }
            }
            return bean;
        } catch (InstantiationException e) {
            throw new ConfigException.BadBean(clazz.getName() + " needs a public no-args constructor to be used as a bean", e);
        } catch (IllegalAccessException e) {
            throw new ConfigException.BadBean(clazz.getName() + " getters and setters are not accessible, they must be for use as a bean", e);
        } catch (InvocationTargetException e) {
            throw new ConfigException.BadBean("Calling bean method on " + clazz.getName() + " caused an exception", e);
        }
    }

    // we could magically make this work in many cases by doing
    // getAnyRef() (or getValue().unwrapped()), but anytime we
    // rely on that, we aren't doing the type conversions Config
    // usually does, and we will throw ClassCastException instead
    // of a nicer error message giving the name of the bad
    // setting. So, instead, we only support a limited number of
    // types plus you can always use Object, ConfigValue, Config,
    // ConfigObject, etc.  as an escape hatch.
    private static Object getValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config,
                                   String configPropName) {
        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            return config.getBoolean(configPropName);
        } else if (parameterClass == Integer.class || parameterClass == int.class) {
            return config.getInt(configPropName);
        } else if (parameterClass == Double.class || parameterClass == double.class) {
            return config.getDouble(configPropName);
        } else if (parameterClass == Long.class || parameterClass == long.class) {
            return config.getLong(configPropName);
        } else if (parameterClass == String.class) {
            return config.getString(configPropName);
        } else if (parameterClass == Duration.class) {
            return config.getDuration(configPropName);
        } else if (parameterClass == ConfigMemorySize.class) {
            return config.getMemorySize(configPropName);
        } else if (parameterClass == Object.class) {
            return config.getAnyRef(configPropName);
        } else if (parameterClass == List.class) {
            return getListValue(beanClass, parameterType, parameterClass, config, configPropName);
        } else if (parameterClass == Map.class) {
            // we could do better here, but right now we don't.
            Type[] typeArgs = ((ParameterizedType) parameterType).getActualTypeArguments();
            if (typeArgs[0] != String.class || typeArgs[1] != Object.class) {
                throw new ConfigException.BadBean("Bean property '" + configPropName + "' of class " + beanClass.getName() + " has unsupported Map<" + typeArgs[0] + "," + typeArgs[1] + ">, only Map<String,Object> is supported right now");
            }
            return config.getObject(configPropName).unwrapped();
        } else if (parameterClass == Config.class) {
            return config.getConfig(configPropName);
        } else if (parameterClass == ConfigObject.class) {
            return config.getObject(configPropName);
        } else if (parameterClass == ConfigValue.class) {
            return config.getValue(configPropName);
        } else if (parameterClass == ConfigList.class) {
            return config.getList(configPropName);
        } else if (hasAtLeastOneBeanProperty(parameterClass)) {
            return createInternal(config.getConfig(configPropName), parameterClass);
        } else {
            throw new ConfigException.BadBean("Bean property " + configPropName + " of class " + beanClass.getName() + " has unsupported type " + parameterType);
        }
    }

    private static Object getListValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config, String configPropName) {
        Type elementType = ((ParameterizedType) parameterType).getActualTypeArguments()[0];

        if (elementType == Boolean.class) {
            return config.getBooleanList(configPropName);
        } else if (elementType == Integer.class) {
            return config.getIntList(configPropName);
        } else if (elementType == Double.class) {
            return config.getDoubleList(configPropName);
        } else if (elementType == Long.class) {
            return config.getLongList(configPropName);
        } else if (elementType == String.class) {
            return config.getStringList(configPropName);
        } else if (elementType == Duration.class) {
            return config.getDurationList(configPropName);
        } else if (elementType == ConfigMemorySize.class) {
            return config.getMemorySizeList(configPropName);
        } else if (elementType == Object.class) {
            return config.getAnyRefList(configPropName);
        } else if (elementType == Config.class) {
            return config.getConfigList(configPropName);
        } else if (elementType == ConfigObject.class) {
            return config.getObjectList(configPropName);
        } else if (elementType == ConfigValue.class) {
            return config.getList(configPropName);
        } else {
            throw new ConfigException.BadBean("Bean property '" + configPropName + "' of class " + beanClass.getName() + " has unsupported list element type " + elementType);
        }
    }

    // null if we can't easily say; this is heuristic/best-effort
    private static ConfigValueType getValueTypeOrNull(Class<?> parameterClass) {
        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            return ConfigValueType.BOOLEAN;
        } else if (parameterClass == Integer.class || parameterClass == int.class) {
            return ConfigValueType.NUMBER;
        } else if (parameterClass == Double.class || parameterClass == double.class) {
            return ConfigValueType.NUMBER;
        } else if (parameterClass == Long.class || parameterClass == long.class) {
            return ConfigValueType.NUMBER;
        } else if (parameterClass == String.class) {
            return ConfigValueType.STRING;
        } else if (parameterClass == Duration.class) {
            return null;
        } else if (parameterClass == ConfigMemorySize.class) {
            return null;
        } else if (parameterClass == List.class) {
            return ConfigValueType.LIST;
        } else if (parameterClass == Map.class) {
            return ConfigValueType.OBJECT;
        } else if (parameterClass == Config.class) {
            return ConfigValueType.OBJECT;
        } else if (parameterClass == ConfigObject.class) {
            return ConfigValueType.OBJECT;
        } else if (parameterClass == ConfigList.class) {
            return ConfigValueType.LIST;
        } else {
            return null;
        }
    }

    private static boolean hasAtLeastOneBeanProperty(Class<?> clazz) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            return false;
        }

        for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
            if (beanProp.getReadMethod() != null && beanProp.getWriteMethod() != null) {
                return true;
            }
        }

        return false;
    }

    private static String toCamelCase(String originalName) {
        String[] words = originalName.split("-+");
        StringBuilder nameBuilder = new StringBuilder(originalName.length());
        for (String word : words) {
            if (nameBuilder.length() == 0) {
                nameBuilder.append(word);
            } else {
                nameBuilder.append(word.substring(0, 1).toUpperCase());
                nameBuilder.append(word.substring(1));
            }
        }
        return nameBuilder.toString();
    }
}
