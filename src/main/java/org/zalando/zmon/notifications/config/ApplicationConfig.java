package org.zalando.zmon.notifications.config;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.zalando.zmon.notifications.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.OAuthTokenInfoService;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.GooglePushNotificationService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.store.NotificationStore;
import org.zalando.zmon.notifications.store.PreSharedKeyStore;
import org.zalando.zmon.notifications.store.RedisNotificationStore;

/**
 * Beans needed in this application.
 * 
 * @author jbellmann
 *
 */
@Configuration
public class ApplicationConfig {

    private final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Autowired
    NotificationServiceConfig config;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    TokenInfoService getTokenInfoService() {
        log.info("Configure OAuthTokenService with url : " + config.getOauthInfoServiceUrl());
        return new OAuthTokenInfoService(config.getOauthInfoServiceUrl());
    }

    @Bean
    PushNotificationService getPushNotificationService() {
        return new GooglePushNotificationService(config.getGooglePushServiceUrl(), config.getGooglePushServiceApiKey());
    }

    @Bean
    NotificationStore getNotificationStore() throws URISyntaxException {
        // JedisPoolConfig poolConfig = new JedisPoolConfig();
        // JedisPool jedisPool = new JedisPool(poolConfig, new
        // URI(config.getRedisUri()));
        return new RedisNotificationStore(null, stringRedisTemplate);
    }

    @Bean
    PreSharedKeyStore getKeyStore(NotificationServiceConfig config) {
        return new PreSharedKeyStore(config.getSharedKeys());
    }
}
