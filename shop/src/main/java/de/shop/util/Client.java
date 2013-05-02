package de.shop.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import static java.lang.annotation.ElementType.PARAMETER;

@Qualifier
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
@Documented
public @interface Client {
}
