package ru.samokat.atlassian.jira.jirabot.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.samokat.atlassian.jira.jirabot.JiraBot;
import ru.samokat.atlassian.jira.jirabot.controller.ActionsController;
import ru.samokat.atlassian.jira.jirabot.controller.CommandController;
import ru.samokat.atlassian.jira.jirabot.controller.MlQuestionController;
import ru.samokat.atlassian.jira.jirabot.controller.PollController;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
@ComponentScan
@Slf4j
public class JiraBotConfiguration {

    @Bean
    public OkHttpClient customOkHttpClient() throws GeneralSecurityException {
        // Create a TrustManager that trusts all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Create a custom SSLContext with the TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create an OkHttpClient with the custom SSLContext
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();

        return client;
    }

    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                }
                super.prepareConnection(connection, httpMethod);
            }
        });

        return restTemplate;
    }

    @Bean
    public JiraBot registerBot(CommandController commandController,
                               PollController pollController,
                               ActionsController actionsController,
                               MlQuestionController mlQuestionController,
                               @Value("${ru.samokat.atlassian.jira.jirabot.token}") String botToken,
                               @Value("${ru.samokat.atlassian.jira.jirabot.username}") String botUserName)
            throws TelegramApiException {
        log.debug("registerBot()");
        JiraBot jiraBot = new JiraBot(commandController,
                                      pollController,
                                      actionsController,
                                      mlQuestionController,
                                      botToken,
                                      botUserName);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(jiraBot);
        jiraBot.giveVoice();
        return jiraBot;
    }

    @Bean Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();

    }
}
