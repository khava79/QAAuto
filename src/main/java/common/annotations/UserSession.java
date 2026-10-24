package common.annotations;

import common.extensions.UserSessionExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(UserSessionExtension.class)

public @interface UserSession {
    int value() default 1;
    int auth() default 1;


}
