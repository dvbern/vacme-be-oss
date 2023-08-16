package ch.dvbern.oss.vacme.logging;

import java.util.concurrent.TimeUnit;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Intercepter dessen InterceptorBinding {@link LoggedIfSlow} verwendet werden kann um zu loggen wenn eine
 * Methode eine hoeehere Laufzeit als die konfigurierte hat
 */
@LoggedIfSlow
@Priority(2000)
@Interceptor
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LoggingIfSlowInterceptor {

	private final VacmeSettingsService vacmeSettingsService;

	@AroundInvoke
	Object logInvocationIfSlow(InvocationContext context) throws Exception {
		StopWatch stopWatch = StopWatch.createStarted();
		Object ret = context.proceed();
		logIfSlow(stopWatch, context);

		return ret;
	}

	private void logIfSlow(@NonNull StopWatch stopwatch, InvocationContext context) {
		stopwatch.stop();
		if (stopwatch.getTime(TimeUnit.MILLISECONDS) > vacmeSettingsService.getLoggingSlowThresholdMs()) {
			String methodname = context.getMethod().toString();
			LOG.warn("VACME-SLOWW-LOGGER: Invocation time for method {} was  {}ms", methodname,
				stopwatch.getTime(TimeUnit.MILLISECONDS));
		}
	}
}
