package ch.dvbern.oss.vacme.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.jax.registration.LatLngJax;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.mchange.v2.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.shared.util.Constants.GEOLOCATION_CONNECTION_TIMEOUT;

@ApplicationScoped
@Slf4j
public class GeocodeService {

	private GeoApiContext context;

	@Inject
	SettingsService settingsService;

	@ConfigProperty(name = "geocode.api.key")
	protected Optional<String> apiKeyOpt;

	@ConfigProperty(name = "vacme.geocode.connection.timeout", defaultValue = GEOLOCATION_CONNECTION_TIMEOUT)
	int geocodeApiTimeout;

	@PostConstruct
	void init() {
		if (isGeocodingActive()) {
			context = new GeoApiContext.Builder()
				.apiKey(apiKeyOpt.get())
				.connectTimeout(geocodeApiTimeout, TimeUnit.SECONDS)
				.readTimeout(geocodeApiTimeout, TimeUnit.SECONDS)
				.build();
		}
	}

	public LatLngJax geocodeAdresse(Adresse adresse) {
		if (!isGeocodingActive()) {
			return new LatLngJax(null, null);
		}
		GeocodingResult[] results;
		try {
			String addrToSearch = adresse.getFullAdresseString();
			results = GeocodingApi.geocode(context, addrToSearch)
				.region("CH")
				.await();
			if (results == null || results.length == 0) {
				return new LatLngJax(null, null);
			}
			if (results.length > 1) {
				LOG.warn("Results length of geocode was {}, potentially inprecise LatLng for addr: {}",
					results.length, adresse.toString());
			}
			return LatLngJax.from(results[0].geometry.location);
		} catch (ApiException | InterruptedException | IOException e) {
			LOG.error("Can't geocode adresse {}, errormsg: {}" , adresse , e.getMessage());
			return new LatLngJax(null, null);
		}

	}

	private boolean isGeocodingActive() {
		return  apiKeyOpt.isPresent() && StringUtils.nonEmptyString(apiKeyOpt.get())
			&& this.settingsService.getSettings().isGeocodingEnabled();
	}

	@PreDestroy
	protected void onDestroy() {
		if (context != null) {
			context.shutdown();
		}
	}
}
