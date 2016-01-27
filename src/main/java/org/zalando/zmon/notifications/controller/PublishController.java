package org.zalando.zmon.notifications.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.zmon.notifications.data.PublishRequestBody;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.store.NotificationStore;
import org.zalando.zmon.notifications.store.PreSharedKeyStore;

@RestController
public class PublishController {

    private final Logger LOG = LoggerFactory.getLogger(PublishController.class);

    private final PreSharedKeyStore keyStore;

    private final PushNotificationService pushNotificationService;

    private final TokenInfoService tokenInfoService;

    private final NotificationStore notificationStore;

    @Autowired
    public PublishController(TokenInfoService tokenInfoService, PushNotificationService pushNotificationService,
            NotificationStore notificationStore, PreSharedKeyStore keyStore) {
        this.tokenInfoService = tokenInfoService;
        this.pushNotificationService = pushNotificationService;
        this.notificationStore = notificationStore;
        this.keyStore = keyStore;
    }

    // publishing new alerts

    @RequestMapping(value = "/api/v1/publish", method = RequestMethod.POST)
    public ResponseEntity<String> publishNotification(@RequestBody PublishRequestBody body,
            @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws IOException {
        boolean authorized = false;

        if (null == oauthHeader) {
            // header not set
        } else if (oauthHeader.startsWith("Bearer")) {
            Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
            if (uid.isPresent()) {
                authorized = true;
            }
        } else if (oauthHeader.startsWith("PreShared")) {
            if (keyStore.isKeyValid(oauthHeader.replace("PreShared ", ""))) {
                authorized = true;
            }
        }

        if (!authorized) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        Collection<String> deviceIds = notificationStore.devicesForAlerts(body.alert_id);

        for (String deviceId : deviceIds) {
            pushNotificationService.push(body, deviceId);
        }

        LOG.info("Sent alert {} to devices {}.", body.alert_id, deviceIds);

        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
