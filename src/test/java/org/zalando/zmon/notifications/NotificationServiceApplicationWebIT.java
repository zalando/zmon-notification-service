package org.zalando.zmon.notifications;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.zalando.zmon.notifications.data.PublishNotificationPart;
import org.zalando.zmon.notifications.data.PublishRequestBody;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { NotificationServiceApplication.class,
        NotificationServiceApplicationWebIT.TestConfiguration.class })
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("it")
public class NotificationServiceApplicationWebIT implements Resources {

    @Value("${local.server.port}")
    private int port;

    private final String TOKEN = "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308";
    private final String DEVICE = UUID.fromString("6f18fb92-dbe3-41ac-ab8e-82be7f30e246").toString();
    private final int ALERT = 142;

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10080);

    @Before
    public void configureWireMockForCheck() throws IOException {
        wireMockRule.stubFor(post(urlPathEqualTo("/gcm/send")).willReturn(aResponse().withStatus(HTTP_OK)));
        wireMockRule.stubFor(get(urlPathEqualTo("/oauth2/tokeninfo"))
                .willReturn(aResponse().withBody(resourceToString(jsonResource("tokeninfo"))).withStatus(HTTP_OK)
                        .withHeader("Content-Type", "application/json")));
    }

    @Test
    public void unauthorized() throws Exception {
        TimeUnit.SECONDS.sleep(3);
        RestTemplate rest = new RestTemplate();
        rest.setErrorHandler(new PassThrough());
        rest.getInterceptors().add(new ClientHttpRequestInterceptor() {

            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                    throws IOException {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, TOKEN);
                return execution.execute(request, body);
            }
        });

        // register
        Map<String, Object> body = Maps.newHashMap();
        body.put("registration_token", DEVICE);
        RequestEntity request = RequestEntity.post(URI.create("http://localhost:" + port + "/api/v1/device"))
                .contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> response = rest.exchange(request, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TimeUnit.SECONDS.sleep(2);

        // subscribe
        Map<String, Object> body2 = Maps.newHashMap();
        body2.put("alert_id", ALERT);
        RequestEntity request2 = RequestEntity.post(URI.create("http://localhost:" + port + "/api/v1/subscription"))
                .contentType(MediaType.APPLICATION_JSON).body(body2);
        ResponseEntity<String> response2 = rest.exchange(request2, String.class);
        Assertions.assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        TimeUnit.SECONDS.sleep(2);

        // publish
        PublishRequestBody rbody = new PublishRequestBody();
        rbody.alert_id = ALERT;
        rbody.entity_id = "customer5.db.zalando";
        PublishNotificationPart part = new PublishNotificationPart();
        part.title = "No database connection to master";
        part.body = "";
        part.icon = "";
        rbody.setNotification(part);

        RequestEntity request3 = RequestEntity.post(URI.create("http://localhost:" + port + "/api/v1/publish"))
                .contentType(MediaType.APPLICATION_JSON).body(rbody);
        ResponseEntity<String> response3 = rest.exchange(request3, String.class);
        Assertions.assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        TimeUnit.SECONDS.sleep(2);
    }

    @Configuration
    static class TestConfiguration {
    }
}
