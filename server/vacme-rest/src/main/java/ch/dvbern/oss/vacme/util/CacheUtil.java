package ch.dvbern.oss.vacme.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@Slf4j
public final class CacheUtil {

	public static final String DEFAULT_CACHE_TIME = "60";

	private CacheUtil() {
		// Prevent instantiation
	}

	public static long getConfiguredTTL(String configName, String cacheTime) {
		try {
			String cachetime = cacheTime;
			if (cachetime == null) {
				LOG.warn("using default config value for {}", configName);
				final Config config = ConfigProvider.getConfig(); // read from static config if not set otherwise,
				// makes testing easier
				cachetime = config.getOptionalValue(configName, String.class).orElse(DEFAULT_CACHE_TIME);
			}
			return Long.parseLong(cachetime);
		} catch (NumberFormatException exception) {
			LOG.error("Missconfiguration: {} must be numeric, using default value " + DEFAULT_CACHE_TIME, configName);
			return Long.parseLong(DEFAULT_CACHE_TIME);
		}
	}
}
