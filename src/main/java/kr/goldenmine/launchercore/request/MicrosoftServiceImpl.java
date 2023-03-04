package kr.goldenmine.launchercore.request;

import kr.goldenmine.launchercore.auth.AuthController;
import kr.goldenmine.launchercore.request.models.MicrosoftTokenResponse;
import kr.goldenmine.launchercore.request.models.minecraft.MinecraftLoginRequest;
import kr.goldenmine.launchercore.request.models.minecraft.MinecraftLoginResponse;
import kr.goldenmine.launchercore.request.models.minecraft.MinecraftProfileResponse;
import kr.goldenmine.launchercore.request.models.xbox.*;
import kr.goldenmine.launchercore.util.LoopUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MicrosoftServiceImpl {
    public static String clientId;
    public static String clientSecret;
    public static int port = 20200;
    public static String state = UUID.randomUUID().toString();

    public static void loadFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            clientId = reader.readLine();
            clientSecret = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MicrosoftTokenResponse refresh(String refreshToken) throws IOException {
        String scope = "XboxLive.signin offline_access";

        Response<MicrosoftTokenResponse> tokenResponse = RetrofitServices.MICROSOFT_SERVICE.requestRefreshToken(
                "consumers",
                clientId,
                scope,
                refreshToken,
                "refresh_token",
                clientSecret
        ).execute();

        return tokenResponse.body();
    }

    public static final String scope = "XboxLive.signin offline_access";

    public static synchronized MicrosoftTokenResponse firstLogin(String id, String password) throws InterruptedException {
        AuthController.future = new CompletableFuture<>();
// https://login.live.com/oauth20_authorize.srf
// prompt=select_account&cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d
// https://login.live.com/oauth20_authorize.srf?
// client_id=bdbbf15c-d072-4d57-be2c-c0702ad18be4&
// redirect_uri=http://localhost:20200/&
// response_type=code&
// scope=XboxLive.signin%20XboxLive.offline_access&
// prompt=select_account&
// cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d
        String url = RetrofitServices.MICROSOFT_LIVE_SERVICE.requestAuthorizationCodeWithCobrandId(
                "select_account",
                "8058f65d-ce06-4c30-9559-473c9275a65d",
                clientId,
                "http://localhost:" + port + "/auth/microsoft",
                "code",
                scope
        ).request().url().url().toString();

        ChromeDriver driver = new ChromeDriver();
        // load login html
        driver.get(url);
        Thread.sleep(1000L);

        Runtime.getRuntime().addShutdownHook(new Thread(driver::quit));

        try {
            WebElement idElement = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "email".equals(it.getAttribute("type"))).findFirst(), 1000L, -1).get();

            Optional<WebElement> optionalSubmitElement = driver.findElements(By.tagName("input")).stream().filter(it -> "submit".equals(it.getAttribute("type"))).findFirst();
            WebElement submitElement = optionalSubmitElement.get();

            idElement.sendKeys(id);
            Thread.sleep(500L);
            submitElement.click();

            WebElement passwordElement = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "password".equals(it.getAttribute("type"))).findFirst(), 1000L, -1).get();
            submitElement = driver.findElements(By.tagName("input")).stream().filter(it -> "submit".equals(it.getAttribute("type"))).findFirst().get();

            passwordElement.sendKeys(password);
            Thread.sleep(500L);
            submitElement.click();

            Optional<WebElement> find = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "button".equals(it.getAttribute("type")) && "아니요".equals(it.getAttribute("value"))).findFirst(), 1000L, 10);

            if (find.isPresent()) {
                WebElement nextButton = driver.findElements(By.tagName("input")).stream().filter(it -> "button".equals(it.getAttribute("type")) && "아니요".equals(it.getAttribute("value"))).findFirst().get();
                nextButton.click();
            }

            String code = AuthController.future.get();

            Response<MicrosoftTokenResponse> tokenResponse = RetrofitServices.MICROSOFT_SERVICE.requestAccessToken(
                    "consumers",
                    clientId,
                    scope,
                    code,
                    "http://localhost:20200/auth/microsoft",
                    "authorization_code",
                    clientSecret
            ).execute();

            log.info(tokenResponse.body().toString());

            return tokenResponse.body();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            driver.quit();
        }

        return null;
    }

    public static MicrosoftTokenResponse firstLoginUsingRefreshToken(String refreshToken) throws IOException {
        /*
        // Line breaks for legibility only

POST /{tenant}/oauth2/v2.0/token HTTP/1.1
Host: https://login.microsoftonline.com
Content-Type: application/x-www-form-urlencoded

client_id=6731de76-14a6-49ae-97bc-6eba6914391e
&scope=https%3A%2F%2Fads.microsoft.com%2Fmsads.manage
&refresh_token=OAAABAAAAiL9Kn2Z27UubvWFPbm0gLWQJVzCTE9UkP3pSx1aXxUjq...
&grant_type=refresh_token
&client_secret=JqQX2PNo9bpM0uEihUPzyrh      // NOTE: Only applicable for web apps
         */
        Response<MicrosoftTokenResponse> tokenResponse = RetrofitServices.MICROSOFT_SERVICE.requestRefreshToken(
                "consumers",
                clientId,
                scope,
                refreshToken,
                "refresh_token",
                clientSecret
        ).execute();

        return tokenResponse.body();
    }

    public static XBoxLiveResponse loginXboxLive(String accessToken) throws IOException {
        XBoxLiveRequest request = new XBoxLiveRequest(
                "http://auth.xboxlive.com",
                "JWT",
                new XBoxLiveRequestProperties(
                        "RPS",
                        "user.auth.xboxlive.com",
                        "d=" + accessToken
                )
        );
        XBoxLiveResponse xboxResponse = RetrofitServices.XBOX_LIVE_SERVICE.authenticate(request).execute().body();

        return xboxResponse;
    }

    public static XBoxXstsResponse loginXboxXsts(XBoxLiveResponse xBoxLiveResponse) throws IOException {
        // xbox login

        String uhs = xBoxLiveResponse.getDisplayClaims().getUhs(null);

        // xsts login
        XBoxXstsRequest xstsRequest = new XBoxXstsRequest(
                "JWT",
                "rp://api.minecraftservices.com/",
                new XBoxXstsRequestProperties(
                        "RETAIL",
                        Collections.singletonList(xBoxLiveResponse.getToken())
                )
        );
        XBoxXstsResponse xstsResponse = RetrofitServices.XBOX_LIVE_XSTS_SERVICE.authenticate(xstsRequest).execute().body();

        // check whether uhs is same
        xstsResponse.getDisplayClaims().getUhs(uhs);

        xstsResponse.setPreviousUhs(uhs);

        return xstsResponse;
    }

    public static MinecraftProfileResponse getMinecraftProfile(MinecraftLoginResponse minecraftLoginResponse) throws IOException {
        MinecraftProfileResponse minecraftProfileResponse = RetrofitServices.MINECRAFT_LOGIN_SERVICE.getProfile(minecraftLoginResponse.getTokenType() + " " + minecraftLoginResponse.getAccessToken()).execute().body();

        return minecraftProfileResponse;
    }

    public static MinecraftLoginResponse getMinecraftLogin(String token, String uhs) throws IOException {
        // minecraft login
        MinecraftLoginRequest minecraftLoginRequest = new MinecraftLoginRequest("XBL3.0 x=" + uhs + ";" + token);
        MinecraftLoginResponse minecraftLoginResponse = RetrofitServices.MINECRAFT_LOGIN_SERVICE.authenticate(minecraftLoginRequest).execute().body();

        return minecraftLoginResponse;
    }

//    public static MinecraftTokenResponse getMinecraftToken(String xstsToken, String uhs) throws IOException {
//        // "identityToken": "XBL3.0 x=<userhash>;<xsts_token>"
//        MinecraftTokenRequest request = new MinecraftTokenRequest("XBL3.0 x=" + uhs + ";" + xstsToken);
//
//        MinecraftTokenResponse response = RetrofitServices.MINECRAFT_LOGIN_SERVICE.getToken(request).execute().body();
//
//        return response;
//    }
}
