package ch.dvbern.oss.vacme.rest_client.well.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Address   {

  @Schema(description = "")
  private String name = null;

  @Schema(description = "")
  private String street = null;

  @Schema(description = "")
  private String zipCode = null;

  @Schema(description = "")
  private String city = null;
 /**
   * Get name
   * @return name
  **/
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Address name(String name) {
    this.name = name;
    return this;
  }

 /**
   * Get street
   * @return street
  **/
  @JsonProperty("street")
  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public Address street(String street) {
    this.street = street;
    return this;
  }

 /**
   * Get zipCode
   * @return zipCode
  **/
  @JsonProperty("zipCode")
  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public Address zipCode(String zipCode) {
    this.zipCode = zipCode;
    return this;
  }

 /**
   * Get city
   * @return city
  **/
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Address city(String city) {
    this.city = city;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    street: ").append(toIndentedString(street)).append("\n");
    sb.append("    zipCode: ").append(toIndentedString(zipCode)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private static String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
