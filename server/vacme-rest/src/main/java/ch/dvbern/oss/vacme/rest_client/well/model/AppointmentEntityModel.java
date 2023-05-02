package ch.dvbern.oss.vacme.rest_client.well.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AppointmentEntityModel   {

  @Schema(description = "")
  private AppointmentEntityModelData data = null;
 /**
   * Get data
   * @return data
  **/
  @JsonProperty("data")
  public AppointmentEntityModelData getData() {
    return data;
  }

  public void setData(AppointmentEntityModelData data) {
    this.data = data;
  }

  public AppointmentEntityModel data(AppointmentEntityModelData data) {
    this.data = data;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppointmentEntityModel {\n");

    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
