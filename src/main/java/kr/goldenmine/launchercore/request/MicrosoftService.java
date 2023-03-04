package kr.goldenmine.launchercore.request;

import kr.goldenmine.launchercore.request.models.MicrosoftTokenResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface MicrosoftService {

    //    @FormUrlEncoded
    @GET("{tenant}/oauth2/v2.0/authorize")
    Call<ResponseBody> requestAuthorizationCode(
            @Path("tenant") String tenant,
            @Query("client_id") String clientId,
            @Query("response_type") String responseType,
            @Query("redirect_uri") String redirectURI,
            @Query("response_mode") String responseMode,
            @Query("scope") String scope,
            @Query("state") String state
//            @Field("code_challenge") String codeChallenge,
//            @Field("code_challenge_method") String codeChallengeMethod
    );

// https://login.live.com/oauth20_authorize.srf
// prompt=select_account&cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d

    @FormUrlEncoded
    @POST("{tenant}/oauth2/v2.0/token")
    Call<MicrosoftTokenResponse> requestAccessToken(
            @Path("tenant") String tenant,
            @Field("client_id") String clientId,
            @Field("scope") String scope,
            @Field("code") String code,
//            @Query("response_type") String responseType,
            @Field("redirect_uri") String redirectURI,
            @Field("grant_type") String grantType,
            @Field("client_secret") String clientSecret
//            @Field("code_challenge") String codeChallenge,
//            @Field("code_challenge_method") String codeChallengeMethod
    );

    @FormUrlEncoded
    @POST("{tenant}/oauth2/v2.0/token")
    Call<MicrosoftTokenResponse> requestRefreshToken(
            @Path("tenant") String tenant,
            @Field("client_id") String clientId,
            @Field("scope") String scope,
//            @Query("response_type") String responseType,
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType,
            @Field("client_secret") String clientSecret
//            @Field("code_challenge") String codeChallenge,
//            @Field("code_challenge_method") String codeChallengeMethod
    );
}
