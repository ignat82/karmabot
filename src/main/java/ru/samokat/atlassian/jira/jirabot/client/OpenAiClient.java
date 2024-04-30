package ru.samokat.atlassian.jira.jirabot.client;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Named
@Slf4j
public class OpenAiClient {
    private final Client client;
    private final String apiKey;

    public OpenAiClient(@Value("${ru.samokat.atlassian.jira.jirabot.openai.token}") String apiKey,
                        @Value("${ru.samokat.atlassian.jira.jirabot.openai.proxy.host}") String proxyHost,
                        @Value("${ru.samokat.atlassian.jira.jirabot.openai.proxy.port}") int proxyPort,
                        @Value("${ru.samokat.atlassian.jira.jirabot.openai.proxy.user:}") String proxyUser,
                        @Value("${ru.samokat.atlassian.jira.jirabot.openai.proxy.password:}") String proxyPassword
                        ) {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());
        config.property(ClientProperties.PROXY_URI,  proxyHost + ":" + proxyPort);
        config.property(ClientProperties.PROXY_USERNAME, proxyUser);
        config.property(ClientProperties.PROXY_PASSWORD,proxyPassword);
        client = ClientBuilder.newClient(config);
        this.apiKey = apiKey;
    }

    public Optional<String> sendQuestion(String prompt) {

        Invocation.Builder builder = client.target("https://api.openai.com/v1/completions")
                                    .request(MediaType.APPLICATION_JSON_TYPE)
                                    .header("Authorization", "Bearer " + apiKey);

        try {
            String response = builder
                    .post(Entity.entity("{\"prompt\": \"" + prompt + "\"}", MediaType.APPLICATION_JSON_TYPE), String.class);
            return Optional.of(response);
        } catch (ForbiddenException e) {
            log.warn("caught forbidden exception while calling openai api. it could be proxy or aut issue" +
                             "exception message is {}", e.getMessage());
            return Optional.empty();
        }

    }

    @PreDestroy
    public void close() {
        client.close();
    }
}
