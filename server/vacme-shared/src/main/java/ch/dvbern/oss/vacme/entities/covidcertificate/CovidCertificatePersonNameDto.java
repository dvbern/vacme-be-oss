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

/*
 * Covid Certificate API Gateway Service
 * Rest API for Covid Certificate API Gateway Service.
 *
 * OpenAPI spec version: 0.0.1
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.dvbern.oss.vacme.entities.covidcertificate;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * CovidCertificatePersonNameDto
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-05-25T17:13:54.751Z[GMT]")
public class CovidCertificatePersonNameDto {
  @JsonProperty("familyName")
  private String familyName = null;

  @JsonProperty("givenName")
  private String givenName = null;

  public CovidCertificatePersonNameDto familyName(String familyName) {
    this.familyName = familyName;
    return this;
  }

   /**
   * family name of the covid certificate owner. Format: maxLength: 50 CHAR
   * @return familyName
  **/
  @Schema(example = "Federer", description = "family name of the covid certificate owner. Format: maxLength: 50 CHAR")
  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public CovidCertificatePersonNameDto givenName(String givenName) {
    this.givenName = givenName;
    return this;
  }

   /**
   * first name of the covid certificate owner. Format: maxLength: 50 CHAR
   * @return givenName
  **/
  @Schema(example = "Roger", description = "first name of the covid certificate owner. Format: maxLength: 50 CHAR")
  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CovidCertificatePersonNameDto covidCertificatePersonNameDto = (CovidCertificatePersonNameDto) o;
    return Objects.equals(this.familyName, covidCertificatePersonNameDto.familyName) &&
        Objects.equals(this.givenName, covidCertificatePersonNameDto.givenName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(familyName, givenName);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CovidCertificatePersonNameDto {\n");

    sb.append("    familyName: ").append(toIndentedString(familyName)).append("\n");
    sb.append("    givenName: ").append(toIndentedString(givenName)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
