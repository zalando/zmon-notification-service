package org.zalando.zmon.notifications;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.push.StubPushNotificationService;

import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { NotificationServiceApplication.class,
        NotificationServiceApplicationWebIT.TestConfiguration.class })
@WebIntegrationTest(randomPort = true)
public class NotificationServiceApplicationWebIT {

    @Value("${local.server.port}")
    private int port;

    private final String DEVICE = UUID.fromString("6f18fb92-dbe3-41ac-ab8e-82be7f30e246").toString();

    @Test
    public void unauthorized() throws Exception {
        TimeUnit.SECONDS.sleep(3);
        RestTemplate rest = new RestTemplate();
        rest.setErrorHandler(new PassThrough());
        Map<String, String> body = Maps.newHashMap();
        body.put("registration_token", DEVICE);
        RequestEntity request = RequestEntity.post(URI.create("http://localhost:" + port + "/api/v1/device"))
                .contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> response = rest.exchange(request, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Configuration
    static class TestConfiguration {

        // @Bean
        // TokenInfoService getTokenInfoService() {
        // return new DummyTokenInfoService();
        // }

        @Bean
        PushNotificationService getPushNotificationService() {
            return new StubPushNotificationService();
        }
    }
}
