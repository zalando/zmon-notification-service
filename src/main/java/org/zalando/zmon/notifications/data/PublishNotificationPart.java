package org.zalando.zmon.notifications.data;

import com.google.common.base.MoreObjects;

// defined by google cloud messaging API
public class PublishNotificationPart {
    public String title;
    public String body;
    public String icon;

    public PublishNotificationPart() {

    }

    public PublishNotificationPart(String t, String b, String i) {
        icon = i;
        body = b;
        title = t;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("title", title).add("body", body).add("icon", icon).toString();
    }
}