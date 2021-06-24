package com.caisheng.cheetah.tools;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caisheng.cheetah.api.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Jsons {
    private static final Logger logger = LoggerFactory.getLogger(Jsons.class);

    public static String toJson(Object bean) {
        try {
            return JSON.toJSONString(bean);
        } catch (Exception ex) {
            logger.error("Jsons.toJson ex bean =" + bean, ex);
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception ex) {
            logger.error("Jsons.toJson ex json =" + json, ex);
        }
        return null;
    }
    public static <T> T fromJson(byte[] bytes, Class<T> clazz) {
            return fromJson(new String(bytes, Constants.UTF_8), clazz);
    }

    public static <T> List<T> fromJsonToLsit(String json, Class<T> clazz) {
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception ex) {
            logger.error("Jsons.toJson ex json =" + json+",class = "+clazz, ex);
        }
        return null;
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            return JSON.parseObject(json, type);
        } catch (Exception ex) {
            logger.error("Jsons.toJson ex json =" + json+",type = "+type, ex);
        }
        return null;
    }

    public static boolean mayJson(String json) {
        if (StringUtils.isBlank(json))
            return false;
        if (json.charAt(0) == '{' && json.charAt(json.length() - 1) == '}')
            return true;
        if (json.charAt(0) == '[' && json.charAt(json.length() - 1) == ']')
            return true;
        return false;
    }

   /* public static String toJson(Map<String, String> map) {
        if (map==null) {
            return "{}";
        }
        return JSON.toJSONString(map);
    }*/

    public static <T,R> String toJson(Map<T,R> map) {
        if (map==null) {
            return "{}";
        }
        return JSON.toJSONString(map);
    }

}
