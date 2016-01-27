package org.zalando.zmon.notifications.store;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisNotificationStore implements NotificationStore {

    private final SetOperations<String, String> setOps;

    public RedisNotificationStore(StringRedisTemplate redisTemplate) {
        this.setOps = redisTemplate.opsForSet();
    }

    /**
     * registers a devices for a specific oauth-uid
     */
    @Override
    public void addDeviceForUid(String deviceId, String uid) {
        setOps.add(devicesForUidKey(uid), deviceId);
    }

    /**
     * registers uids to be notified for specific alert
     */
    @Override
    public void addAlertForUid(int alertId, String uid) {
        setOps.add(alertsForUidKey(uid), "" + alertId);
        setOps.add(notificationsForAlertKey(alertId), uid);
    }

    /**
     * removes a device for a uid
     */
    @Override
    public void removeDeviceForUid(String deviceId, String uid) {
        setOps.remove(devicesForUidKey(uid), deviceId);
    }

    @Override
    public void removeAlertForUid(int alertId, String uid) {
        setOps.remove(alertsForUidKey(uid), "" + alertId);
        setOps.remove(notificationsForAlertKey(alertId), uid);
    }

    @Override
    public Collection<Integer> alertsForUid(String uid) {
        return setOps.members(alertsForUidKey(uid)).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    // @formatter:off
    @Override
    public Collection<String> devicesForAlerts(int alertId) {

        HashSet<String> deviceIds = new HashSet<>();
        List<CompletableFuture<Set<String>>> devicesForUids = setOps.members(notificationsForAlertKey(alertId))
                                                                    .stream()
                                                                    .map(uid -> CompletableFuture.supplyAsync(() -> setOps.members(devicesForUidKey(uid))))
                                                                    .collect(Collectors.toList());

        devicesForUids.stream().map(CompletableFuture::join).forEach(set -> deviceIds.addAll(set));
        return deviceIds;
    }
    // @formatter:on

    // helpers

    // build redis key for sets containing all devices for a given uid
    private String devicesForUidKey(String uid) {
        return String.format("zmon:push:user-devices:%s", uid);
    }

    private String alertsForUidKey(String uid) {
        return "zmon:push:alerts-for-uid:" + uid;
    }

    // build redis key for sets containing all devices subscribed to given
    // alertId
    private String notificationsForAlertKey(int alertId) {
        return String.format("zmon:push:uids-for-alert:%d", alertId);
    }
}
