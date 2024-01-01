package me.quiu.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ildar (quiu)
 * <p>Priority creation on 1/2/2024 at 2:45 AM</p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    int value() default 0;

}
