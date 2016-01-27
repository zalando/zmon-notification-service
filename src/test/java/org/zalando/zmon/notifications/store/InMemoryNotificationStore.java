package org.zalando.zmon.notifications.store;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class InMemoryNotificationStore implements NotificationStore {
    private final Multimap<String, String> devices = ArrayListMultimap.create();
    private final Multimap<Integer, String> alerts = ArrayListMultimap.create();
    private final Multimap<String, Integer> alertsForUid = ArrayListMultimap.create();

    @Override
    public void addDeviceForUid(String deviceId, String uid) {
        devices.put(uid, deviceId);
    }

    @Override
    public void addAlertForUid(int alertId, String uid) {
        alerts.put(alertId, uid);
    }

    @Override
    public void removeDeviceForUid(String deviceId, String uid) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void removeAlertForUid(int alertId, String uid) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Collection<Integer> alertsForUid(String uid) {
        return alertsForUid.get(uid);
    }

    @Override
    public Collection<String> devicesForAlerts(int alertId) {
        ArrayList<String> result = new ArrayList<>();
        for (String uid : alerts.get(alertId)) {
            result.addAll(devices.get(uid));
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("in-mem-store: devices=%s alerts=%s", devices, alerts);
    }
}
