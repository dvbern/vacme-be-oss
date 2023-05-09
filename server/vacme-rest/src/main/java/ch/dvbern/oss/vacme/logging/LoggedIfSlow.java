package ch.dvbern.oss.vacme.logging;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is the interceptor Binding for {@link LoggingIfSlowInterceptor}
 */
@InterceptorBinding
@Retention(value = RUNTIME)
@Target(value = { METHOD, TYPE })
public @interface LoggedIfSlow {
}
