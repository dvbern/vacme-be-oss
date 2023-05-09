package ch.dvbern.oss.vacme.rest_client.well.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.oss.vacme.rest_client.well.api.auth.WellAPIConstants.WELL_API_DATE_FORMAT;

public class VacMeAppointmentRequestDto   {

  @Schema(required = true, description = "")
  private String vacmeAppointmentId = null;

  @Schema(required = true, description = "")
  private UUID userId = null;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = WELL_API_DATE_FORMAT) // manually added
  @Schema(required = true, description = "")
  private Date appointmentStart = null;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = WELL_API_DATE_FORMAT) // manually added
  @Schema(required = true, description = "")
  private Date appointmentEnd = null;

  @Schema(required = true, description = "")
  private Address address = null;

  @Schema(required = true, description = "")
  private String diseaseName = null;

  @Schema(required = true, description = "")
  private Integer doseNumber = null;
 /**
   * Get vacmeAppointmentId
   * @return vacmeAppointmentId
  **/
  @JsonProperty("vacmeAppointmentId")
  public String getVacmeAppointmentId() {
    return vacmeAppointmentId;
  }

  public void setVacmeAppointmentId(String vacmeAppointmentId) {
    this.vacmeAppointmentId = vacmeAppointmentId;
  }

  public VacMeAppointmentRequestDto vacmeAppointmentId(String vacmeAppointmentId) {
    this.vacmeAppointmentId = vacmeAppointmentId;
    return this;
  }

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

  public VacMeAppointmentRequestDto userId(UUID userId) {
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

  public VacMeAppointmentRequestDto appointmentStart(Date appointmentStart) {
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

  public VacMeAppointmentRequestDto appointmentEnd(Date appointmentEnd) {
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

  public VacMeAppointmentRequestDto address(Address address) {
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

  public VacMeAppointmentRequestDto diseaseName(String diseaseName) {
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

  public VacMeAppointmentRequestDto doseNumber(Integer doseNumber) {
    this.doseNumber = doseNumber;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VacMeAppointmentRequestDto {\n");

    sb.append("    vacmeAppointmentId: ").append(toIndentedString(vacmeAppointmentId)).append("\n");
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
