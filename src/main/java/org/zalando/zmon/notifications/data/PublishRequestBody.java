package org.zalando.zmon.notifications.data;

import com.google.common.base.MoreObjects;

public class PublishRequestBody {
    public int alert_id;
    public String entity_id;
    public PublishNotificationPart notification;

    public PublishNotificationPart getNotification() {
        return notification;
    }

    public void setNotification(PublishNotificationPart notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("alert_id", alert_id).add("notification", notification)
                .add("entity_id", entity_id).toString();
    }
}