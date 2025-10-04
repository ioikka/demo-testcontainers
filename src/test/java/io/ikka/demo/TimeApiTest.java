package io.ikka.demo;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class TimeApiTest {

    @SuppressWarnings("resource")
    @Container
    private static final GenericContainer<?> timeApiContainer = new GenericContainer<>(
            DockerImageName.parse("belgampaul/time-api:latest"))
            .withExposedPorts(8080);

    @Test
    void testTimeApiEndpoint() throws Exception {
        // Get the mapped port and host for the container
        String host = timeApiContainer.getHost();
        Integer port = timeApiContainer.getFirstMappedPort();
        @SuppressWarnings("HttpUrlsUsage")
        String url = String.format("http://%s:%d/time", host, port);

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, response.statusCode());
    }
}