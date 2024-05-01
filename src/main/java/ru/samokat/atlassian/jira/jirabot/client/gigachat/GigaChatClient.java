package ru.samokat.atlassian.jira.jirabot.client.gigachat;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@Slf4j
public class GigaChatClient {
    private final Gson gson;
    private final OkHttpClient okHttpClient;
    private final GigaChatTokenProvider tokenProvider;
    private final String questionUri;


    public GigaChatClient(Gson gson,
                          OkHttpClient customOkHttpClient,
                          GigaChatTokenProvider gigaChatTokenProvider,
                          @Value("${ru.samokat.atlassian.jira.jirabot.gigachat.uri.completion}") String questionUri) {
        this.gson = gson;
        this.okHttpClient = customOkHttpClient;
        this.tokenProvider = gigaChatTokenProvider;
        this.questionUri = questionUri;
    }

    public Optional<String> sendQuestion(String prompt) {
        log.debug("sendQuestion({})", prompt);

        GigaChatRequest gigaChatRequest = GigaChatRequest.constructSimpleRequest(prompt);
        RequestBody requestBody = RequestBody.create(gson.toJson(gigaChatRequest).getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(questionUri)
                .method("POST", requestBody)
                .addHeader("Authorization", tokenProvider.getToken())
                .build();

        try {
            Response httpResponse = okHttpClient.newCall(request).execute();
            String responseBody = httpResponse.body().string();
            GigaChatResponse response = gson.fromJson(responseBody, GigaChatResponse.class);
            log.trace("got GigaChat response. returning");
            return response.getFirstMessage();
        } catch (Exception e) {
            String errormessage = String.format("caught exception");
            log.warn(errormessage, e);
            throw new RuntimeException(errormessage);
        }
    }

}
