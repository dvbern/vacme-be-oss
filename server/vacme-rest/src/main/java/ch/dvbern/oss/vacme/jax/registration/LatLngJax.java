package ch.dvbern.oss.vacme.jax.registration;

import com.google.maps.model.LatLng;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LatLngJax {

	@Nullable
	private Double lat;

	@Nullable
	private Double lng;

	public static LatLngJax from(LatLng latLng) {
		return new LatLngJax(latLng.lat, latLng.lng);
	}
}
