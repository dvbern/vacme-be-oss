package ch.dvbern.oss.vacme.rest_client.well.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.oss.vacme.rest_client.well.api.auth.WellAPIConstants.WELL_API_DATE_FORMAT;

public class VacMeApprovalPeriodRequestDto   {

  @Schema(required = true, description = "")
  private String vacmeApprovalPeriodId = null;

  @Schema(required = true, description = "")
  private UUID userId = null;

  @Schema(required = true, description = "")
  private String diseaseName = null;

  @Schema(description = "")
  private Integer doseNumber = null;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = WELL_API_DATE_FORMAT) // manually added
  @Schema(required = true, description = "")
  private Date approvalPeriodDate = null;
 /**
   * Get vacmeApprovalPeriodId
   * @return vacmeApprovalPeriodId
  **/
  @JsonProperty("vacmeApprovalPeriodId")
  public String getVacmeApprovalPeriodId() {
    return vacmeApprovalPeriodId;
  }

  public void setVacmeApprovalPeriodId(String vacmeApprovalPeriodId) {
    this.vacmeApprovalPeriodId = vacmeApprovalPeriodId;
  }

  public VacMeApprovalPeriodRequestDto vacmeApprovalPeriodId(String vacmeApprovalPeriodId) {
    this.vacmeApprovalPeriodId = vacmeApprovalPeriodId;
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

  public VacMeApprovalPeriodRequestDto userId(UUID userId) {
    this.userId = userId;
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

  public VacMeApprovalPeriodRequestDto diseaseName(String diseaseName) {
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

  public VacMeApprovalPeriodRequestDto doseNumber(Integer doseNumber) {
    this.doseNumber = doseNumber;
    return this;
  }

 /**
   * Get approvalPeriodDate
   * @return approvalPeriodDate
  **/
  @JsonProperty("approvalPeriodDate")
  public Date getApprovalPeriodDate() {
    return approvalPeriodDate;
  }

  public void setApprovalPeriodDate(Date approvalPeriodDate) {
    this.approvalPeriodDate = approvalPeriodDate;
  }

  public VacMeApprovalPeriodRequestDto approvalPeriodDate(Date approvalPeriodDate) {
    this.approvalPeriodDate = approvalPeriodDate;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VacMeApprovalPeriodRequestDto {\n");

    sb.append("    vacmeApprovalPeriodId: ").append(toIndentedString(vacmeApprovalPeriodId)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    diseaseName: ").append(toIndentedString(diseaseName)).append("\n");
    sb.append("    doseNumber: ").append(toIndentedString(doseNumber)).append("\n");
    sb.append("    approvalPeriodDate: ").append(toIndentedString(approvalPeriodDate)).append("\n");
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
