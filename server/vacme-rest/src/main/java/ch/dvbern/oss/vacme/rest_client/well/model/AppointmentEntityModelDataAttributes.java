package ch.dvbern.oss.vacme.rest_client.well.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AppointmentEntityModelDataAttributes   {

  @Schema(description = "")
  private UUID userId = null;

  @Schema(description = "")
  private Date appointmentStart = null;

  @Schema(description = "")
  private Date appointmentEnd = null;

  @Schema(description = "")
  private Address address = null;

  @Schema(description = "")
  private String diseaseName = null;

  @Schema(description = "")
  private Integer doseNumber = null;
 /**
   * Get userId
   * @return userId
  **/
  @JsonProperty("userId")
  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public AppointmentEntityModelDataAttributes userId(UUID userId) {
    this.userId = userId;
    return this;
  }

 /**
   * Get appointmentStart
   * @return appointmentStart
  **/
  @JsonProperty("appointmentStart")
  public Date getAppointmentStart() {
    return appointmentStart;
  }

  public void setAppointmentStart(Date appointmentStart) {
    this.appointmentStart = appointmentStart;
  }

  public AppointmentEntityModelDataAttributes appointmentStart(Date appointmentStart) {
    this.appointmentStart = appointmentStart;
    return this;
  }

 /**
   * Get appointmentEnd
   * @return appointmentEnd
  **/
  @JsonProperty("appointmentEnd")
  public Date getAppointmentEnd() {
    return appointmentEnd;
  }

  public void setAppointmentEnd(Date appointmentEnd) {
    this.appointmentEnd = appointmentEnd;
  }

  public AppointmentEntityModelDataAttributes appointmentEnd(Date appointmentEnd) {
    this.appointmentEnd = appointmentEnd;
    return this;
  }

 /**
   * Get address
   * @return address
  **/
  @JsonProperty("address")
  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public AppointmentEntityModelDataAttributes address(Address address) {
    this.address = address;
    return this;
  }

 /**
   * Get diseaseName
   * @return diseaseName
  **/
  @JsonProperty("diseaseName")
  public String getDiseaseName() {
    return diseaseName;
  }

  public void setDiseaseName(String diseaseName) {
    this.diseaseName = diseaseName;
  }

  public AppointmentEntityModelDataAttributes diseaseName(String diseaseName) {
    this.diseaseName = diseaseName;
    return this;
  }

 /**
   * Get doseNumber
   * minimum: 1
   * @return doseNumber
  **/
  @JsonProperty("doseNumber")
  public Integer getDoseNumber() {
    return doseNumber;
  }

  public void setDoseNumber(Integer doseNumber) {
    this.doseNumber = doseNumber;
  }

  public AppointmentEntityModelDataAttributes doseNumber(Integer doseNumber) {
    this.doseNumber = doseNumber;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppointmentEntityModelDataAttributes {\n");

    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    appointmentStart: ").append(toIndentedString(appointmentStart)).append("\n");
    sb.append("    appointmentEnd: ").append(toIndentedString(appointmentEnd)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    diseaseName: ").append(toIndentedString(diseaseName)).append("\n");
    sb.append("    doseNumber: ").append(toIndentedString(doseNumber)).append("\n");
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
