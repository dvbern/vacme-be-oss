package ch.dvbern.oss.vacme.logging;

import java.util.concurrent.TimeUnit;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.shared.util.Constants.DB_QUERY_SLOW_THRESHOLD;

/**
 * Intercepter dessen InterceptorBinding {@link LoggedIfSlow} verwendet werden kann um zu loggen wenn eine
 * Methode eine hoeehere Laufzeit als die konfigurierte hat
 */
@LoggedIfSlow
@Priority(2000)
@Interceptor
@Slf4j
public class LoggingIfSlowInterceptor {


	@ConfigProperty(name = "vacme.loggingintercepot.slowthreshold.ms", defaultValue =  DB_QUERY_SLOW_THRESHOLD)
	long slowThresholdMs;

	@AroundInvoke
	Object logInvocationIfSlow(InvocationContext context) throws Exception {
		StopWatch stopWatch = StopWatch.createStarted();
		Object ret = context.proceed();
		logIfSlow(stopWatch, context);

		return ret;
	}

	private void logIfSlow(@NonNull StopWatch stopwatch, InvocationContext context) {
		stopwatch.stop();
		if (stopwatch.getTime(TimeUnit.MILLISECONDS) > slowThresholdMs) {
			String methodname = context.getMethod().toString();
			LOG.warn("VACME-SLOWW-LOGGER: Invocation time for method {} was  {}ms", methodname,
				stopwatch.getTime(TimeUnit.MILLISECONDS));
		}
	}
}
