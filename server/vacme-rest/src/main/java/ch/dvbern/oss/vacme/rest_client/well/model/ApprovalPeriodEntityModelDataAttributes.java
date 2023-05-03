package ch.dvbern.oss.vacme.rest_client.well.model;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ApprovalPeriodEntityModelDataAttributes   {

  @Schema(description = "")
  private UUID userId = null;

  @Schema(description = "")
  private Date approvalPeriodDate = null;

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

  public ApprovalPeriodEntityModelDataAttributes userId(UUID userId) {
    this.userId = userId;
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

  public ApprovalPeriodEntityModelDataAttributes approvalPeriodDate(Date approvalPeriodDate) {
    this.approvalPeriodDate = approvalPeriodDate;
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

  public ApprovalPeriodEntityModelDataAttributes diseaseName(String diseaseName) {
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

  public ApprovalPeriodEntityModelDataAttributes doseNumber(Integer doseNumber) {
    this.doseNumber = doseNumber;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApprovalPeriodEntityModelDataAttributes {\n");

    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    approvalPeriodDate: ").append(toIndentedString(approvalPeriodDate)).append("\n");
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
