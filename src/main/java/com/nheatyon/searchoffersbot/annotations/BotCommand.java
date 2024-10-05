package com.nheatyon.searchoffersbot.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {

    String value();
    int args() default 1;
    String syntax() default "";
    boolean adminCommand() default false;
    boolean variableArgs() default false;
    boolean async() default true;
}
