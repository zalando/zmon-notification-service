package org.zalando.zmon.notifications.controller;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.zmon.notifications.data.DeviceRequestBody;
import org.zalando.zmon.notifications.data.SubscriptionRequestBody;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.store.NotificationStore;

@RestController
public class DeviceController {

    private final Logger LOG = LoggerFactory.getLogger(DeviceController.class);

    private final TokenInfoService tokenInfoService;

    private final NotificationStore notificationStore;

    @Autowired
    public DeviceController(TokenInfoService tokenInfoService, NotificationStore notificationStore) {
        this.tokenInfoService = tokenInfoService;
        this.notificationStore = notificationStore;
    }

    @RequestMapping(value = "/api/v1/device", method = RequestMethod.POST)
    public ResponseEntity<String> registerDevice(@RequestBody DeviceRequestBody body,
            @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {

            if (body.registration_token == null || "".equals(body.registration_token)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.addDeviceForUid(body.registration_token, uid.get());
            LOG.info("Registered device {} for uid {}.", body.registration_token, uid.get());

            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/device/{registration_token}", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterDevice(@PathVariable(value = "registration_token") String registrationToken,
            @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {

            if (registrationToken == null || "".equals(registrationToken)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.removeDeviceForUid(registrationToken, uid.get());

            LOG.info("Removed device {} for uid {}.", registrationToken, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/subscription", method = RequestMethod.POST)
    public ResponseEntity<String> registerSubscription(@RequestBody SubscriptionRequestBody body,
            @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.addAlertForUid(body.alert_id, uid.get());
            LOG.info("Registered alert {} for uid {}.", body.alert_id, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/subscription/{alert_id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterSubscription(@PathVariable(value = "alert_id") int alertId,
            @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.removeAlertForUid(alertId, uid.get());
            LOG.info("Removed alert {} for uid {}.", alertId, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        }
        return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/api/v1/user/subscriptions", method = RequestMethod.GET)
    public ResponseEntity<Collection<Integer>> getRegisteredAlerts(
            @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            return new ResponseEntity<>(notificationStore.alertsForUid(uid.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>((Collection<Integer>) null, HttpStatus.UNAUTHORIZED);
    }

}
