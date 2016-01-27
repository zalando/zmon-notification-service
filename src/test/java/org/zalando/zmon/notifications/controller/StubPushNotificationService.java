package org.zalando.zmon.notifications.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;

import org.zalando.zmon.notifications.data.PublishRequestBody;
import org.zalando.zmon.notifications.push.PushNotificationService;

public class StubPushNotificationService implements PushNotificationService {

    private final Multimap<String, PublishRequestBody> sent = ArrayListMultimap.create();

    @Override
    public void push(PublishRequestBody notification, String deviceToken) throws IOException {
        sent.put(deviceToken, notification);
    }

    @Override
    public String toString() {
        return "stub-pushed-notifications: " + sent;
    }
}
