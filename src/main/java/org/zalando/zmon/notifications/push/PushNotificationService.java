package org.zalando.zmon.notifications.push;

import java.io.IOException;

import org.zalando.zmon.notifications.data.PublishRequestBody;

public interface PushNotificationService {

    void push(PublishRequestBody notification, String deviceToken) throws IOException;

}
