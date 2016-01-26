package org.zalando.zmon.notifications.push;

import static org.zalando.zmon.notifications.json.JsonHelper.jsonEntityFor;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.zalando.zmon.notifications.data.PublishRequestBody;

import com.google.common.base.MoreObjects;

public class GooglePushNotificationService implements PushNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(GooglePushNotificationService.class);

    private final String serviceUrl;
    private final String googleApiKey;

    public GooglePushNotificationService(String serviceUrl, String googleApiKey) {
        LOG.info("Setting Google Service Url to {}", serviceUrl);
        LOG.info("Setting Google API Key to ...{}", googleApiKey.substring(Math.max(0, googleApiKey.length() - 4)));

        this.serviceUrl = serviceUrl;
        this.googleApiKey = googleApiKey;
    }

    @Async
    @Override
    public void push(PublishRequestBody notification, String deviceToken) throws IOException {
        Response response = Request.Post(serviceUrl).addHeader("Authorization", "Key=" + googleApiKey)
                .body(jsonEntityFor(deviceToken, notification)).execute();

        LOG.info("Request sent to google with response code={}", response.returnResponse().getStatusLine());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("serviceUrl", serviceUrl)
                .add("googleApiKey", "..." + googleApiKey.substring(Math.max(0, googleApiKey.length() - 4))).toString();
    }
}
