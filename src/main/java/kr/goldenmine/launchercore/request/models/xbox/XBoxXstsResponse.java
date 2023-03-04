package kr.goldenmine.launchercore.request.models.xbox;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class XBoxXstsResponse extends MicrosoftErrorResponse {
    @SerializedName("IssueInstant")
    String issueInstant;

    @SerializedName("NotAfter")
    String notAfter;

    @SerializedName("Token")
    String token;

    @SerializedName("DisplayClaims")
    XBoxXstsResponseDisplayClaims displayClaims;

    String previousUhs;

    public void setPreviousUhs(String previousUhs) {
        this.previousUhs = previousUhs;
    }

    @Override
    public String toString() {
        return "XBoxXstsResponse{" +
                "issueInstant='" + issueInstant + '\'' +
                ", notAfter='" + notAfter + '\'' +
                ", token='" + token + '\'' +
                ", displayClaims=" + displayClaims +
                ", previousUhs='" + previousUhs + '\'' +
                ", errorCode=" + errorCode +
                ", message='" + message + '\'' +
                ", redirectUrl='" + redirectUrl + '\'' +
                '}';
    }
}