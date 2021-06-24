package com.caisheng.cheetah.api.spi;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Spi {
    String value() default "";
    int sort() default 0;
}
