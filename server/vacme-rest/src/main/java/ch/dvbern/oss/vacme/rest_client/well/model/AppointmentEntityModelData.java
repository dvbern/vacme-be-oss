package ch.dvbern.oss.vacme.rest_client.well.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AppointmentEntityModelData   {

  @Schema(description = "")
  private String id = null;

  @Schema(description = "")
  private String type = null;

  @Schema(description = "")
  private AppointmentEntityModelDataAttributes attributes = null;
 /**
   * Get id
   * @return id
  **/
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AppointmentEntityModelData id(String id) {
    this.id = id;
    return this;
  }

 /**
   * Get type
   * @return type
  **/
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public AppointmentEntityModelData type(String type) {
    this.type = type;
    return this;
  }

 /**
   * Get attributes
   * @return attributes
  **/
  @JsonProperty("attributes")
  public AppointmentEntityModelDataAttributes getAttributes() {
    return attributes;
  }

  public void setAttributes(AppointmentEntityModelDataAttributes attributes) {
    this.attributes = attributes;
  }

  public AppointmentEntityModelData attributes(AppointmentEntityModelDataAttributes attributes) {
    this.attributes = attributes;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppointmentEntityModelData {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
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
