package ch.dvbern.oss.vacme.rest_client.well.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AccountLinkEntityModelDataAttributes   {
  public enum LinkStatusEnum {
    LINKED("LINKED"),
    UNLINKED("UNLINKED");

    private String value;

    LinkStatusEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static LinkStatusEnum fromValue(String text) {
      for (LinkStatusEnum b : LinkStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @Schema(description = "")
  private LinkStatusEnum linkStatus = null;
 /**
   * Get linkStatus
   * @return linkStatus
  **/
  @JsonProperty("linkStatus")
  public String getLinkStatus() {
    if (linkStatus == null) {
      return null;
    }
    return linkStatus.getValue();
  }

  public void setLinkStatus(LinkStatusEnum linkStatus) {
    this.linkStatus = linkStatus;
  }

  public AccountLinkEntityModelDataAttributes linkStatus(LinkStatusEnum linkStatus) {
    this.linkStatus = linkStatus;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountLinkEntityModelDataAttributes {\n");

    sb.append("    linkStatus: ").append(toIndentedString(linkStatus)).append("\n");
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
