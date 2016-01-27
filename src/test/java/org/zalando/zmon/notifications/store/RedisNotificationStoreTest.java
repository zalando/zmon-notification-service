package org.zalando.zmon.notifications.store;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.zalando.zmon.notifications.AbstractSpringTest;
import org.zalando.zmon.notifications.NotificationServiceConfig;
import org.zalando.zmon.notifications.RedisServerRule;
import org.zalando.zmon.notifications.config.ApplicationConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

@ContextConfiguration
public class RedisNotificationStoreTest extends AbstractSpringTest {

    private final String DEVICE = UUID.fromString("6f18fb92-dbe3-41ac-ab8e-82be7f30e246").toString();
    private final int ALERT = 142;

    @ClassRule
    public static RedisServerRule redisServerRule = new RedisServerRule();

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10080);

    @Autowired
    private RedisNotificationStore redisNotificationStore;

    @Before
    public void configureWireMockForCheck() throws IOException {
        wireMockRule.stubFor(post(urlPathEqualTo("/gcm/send")).willReturn(aResponse().withStatus(HTTP_OK)));
    }

    @Test
    public void lookUp() throws InterruptedException {
        redisNotificationStore.addAlertForUid(ALERT, DEVICE);
        redisNotificationStore.addDeviceForUid(DEVICE, DEVICE);
        TimeUnit.MILLISECONDS.sleep(200);

        Collection<String> result = redisNotificationStore.devicesForAlerts(ALERT);
        Assertions.assertThat(result).containsExactly(DEVICE);
    }

    @Configuration
    @Import({ RedisAutoConfiguration.class, ApplicationConfig.class })
    // @EnableConfigurationProperties(NotificationServiceConfig.class)
    static class TestConfiguration {

        @Bean
        public NotificationServiceConfig config() {
            NotificationServiceConfig c = new NotificationServiceConfig();
            c.setGooglePushServiceApiKey("GEHEIM");
            c.setGooglePushServiceUrl("https://localhost:10080/gcm/send");
            return c;
        }
    }

}
