package kr.goldenmine.launchercore.request;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface MicrosoftLiveService {
// https://login.live.com/oauth20_authorize.srf?
// client_id=bdbbf15c-d072-4d57-be2c-c0702ad18be4&
// redirect_uri=http://localhost:20200/&
// response_type=code&
// scope=XboxLive.signin%20XboxLive.offline_access&
// prompt=select_account&
// cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d
    @GET("oauth20_authorize.srf")
    Call<ResponseBody> requestAuthorizationCodeWithCobrandId(
            @Query("prompt") String prompt,
            @Query("cobrandid") String cobrandId,
            @Query("client_id") String clientId,
            @Query("redirect_uri") String redirectURI,
            @Query("response_type") String responseType,
            @Query("scope") String scope
    );

}
