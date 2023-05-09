/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.entities.covidcertificate;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CovidCertificatePersonNameDto
 */

public class CovidCertificateAdresseDto {
  @JsonProperty("streetAndNr")
  private String streetAndNr = null;

  @JsonProperty("zipCode")
  private String zipCode = null;

  @JsonProperty("city")
  private String city = null;

  @JsonProperty("cantonCodeSender")
  private String cantonCodeSender = null;


	public String getStreetAndNr() {
		return streetAndNr;
	}

	public void setStreetAndNr(String streetAndNr) {
		this.streetAndNr = streetAndNr;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCantonCodeSender() {
		return cantonCodeSender;
	}

	public void setCantonCodeSender(String cantonCodeSender) {
		this.cantonCodeSender = cantonCodeSender;
	}

	@Override
	public String toString() {
		return "CovidCertificateAdresseDto{" +
			"streetAndNr='" + streetAndNr + '\'' +
			", zipCode='" + zipCode + '\'' +
			", city='" + city + '\'' +
			", cantonCodeSender='" + cantonCodeSender + '\'' +
			'}';
	}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
	  CovidCertificateAdresseDto covidCertificatePersonNameDto = (CovidCertificateAdresseDto) o;
	  return Objects.equals(this.city, covidCertificatePersonNameDto.city) &&
		  Objects.equals(this.zipCode, covidCertificatePersonNameDto.zipCode) &&
		  Objects.equals(this.cantonCodeSender, covidCertificatePersonNameDto.cantonCodeSender) &&
		  Objects.equals(this.streetAndNr, covidCertificatePersonNameDto.streetAndNr);
  }

	@Override
	public int hashCode() {
		return Objects.hash(streetAndNr, zipCode, city, cantonCodeSender);
	}
}
