
package com.caisheng.cheetah.api.spi.common;


public interface Json {
    Json JSON = JsonFactory.create();

    <T> T fromJson(String json, Class<T> clazz);

    String toJson(Object json);
}
