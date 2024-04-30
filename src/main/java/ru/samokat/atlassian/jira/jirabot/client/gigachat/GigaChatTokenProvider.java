package ru.samokat.atlassian.jira.jirabot.client.gigachat;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Named
@Slf4j
public class GigaChatTokenProvider {
    private final String oauthUri;
    private final String basicAuthString;
    private final OkHttpClient okHttpClient;
    private final Gson gson;
    private final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    private final String UID_HEADER_KEY = "RqUID";
    private long liveTime;
    private String token;

    {
        body.clear();
        body.add("scope", "GIGACHAT_API_PERS");
    }

    public GigaChatTokenProvider(@Value("${ru.samokat.atlassian.jira.jirabot.gigachat.uri.oauth}") String oauthUri,
                                 @Value("${ru.samokat.atlassian.jira.jirabot.gigachat.client.id}") String clientId,
                                 @Value("${ru.samokat.atlassian.jira.jirabot.gigachat.client.secret}") String clientSecret,
                                 OkHttpClient okHttpClient,
                                 Gson gson) {
        this.oauthUri = oauthUri;
        this.basicAuthString = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        this.okHttpClient = okHttpClient;
        this.gson = gson;
    }
    public String getToken() {
        log.debug("getToken()");
        if (liveTime < Instant.now().toEpochMilli()) {
            renewToken();
        }
        return token;
    }

    private void renewToken() {
        log.debug("renewToken()");

        FormBody requestBody = new FormBody.Builder()
                .add("scope", "GIGACHAT_API_PERS")
                .build();

        Request request = new Request.Builder()
                .url(oauthUri)
                .method("POST", requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader(UID_HEADER_KEY, UUID.randomUUID().toString())
                .addHeader("Authorization", basicAuthString)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            TokenResponse tokenResponse = gson.fromJson(responseBody, TokenResponse.class);
            token = "Bearer " + tokenResponse.accessToken;
            liveTime = tokenResponse.expiresAt;
        } catch (Exception e) {
            String errormessage = String.format("caught an exception while getting GigaChat token. " +
                    "Check gigachat credentials, network connectivity and GigaChat uri");
            log.warn(errormessage, e);
            throw new RuntimeException(errormessage);
        }

    }
    private static class TokenResponse {
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("expires_at")
        private long expiresAt;
    }
}


