package org.zalando.zmon.notifications.opsgenie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.opsgenie.action.ActionType;
import org.zalando.zmon.notifications.opsgenie.action.AlertAction;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;

import java.util.List;

import static org.zalando.zmon.notifications.ZMonEventType.*;

/**
 * Created by mabdelhameed on 21/06/2017.
 */
public class ActionHandler {
    private final Logger log = LoggerFactory.getLogger(ActionHandler.class);

    private final AlertStore alertStore;
    private final HttpEventLogger eventLog;
    private final NotificationServiceConfig config;

    public ActionHandler(final NotificationServiceConfig config, AlertStore alertStore, HttpEventLogger eventLog) {
        this.alertStore = alertStore;
        this.eventLog = eventLog;
        this.config = config;
    }

    public void handleAction(final AlertAction action) {

        int alertId = action.getAlertId();
        String username = action.getAlert().getUsername();
        ActionType actionType = action.getAction();

        if (actionType == ActionType.CREATE) {
            handleCreate(alertId, action.getAlert().getRecipients());
        } else if (actionType == ActionType.CLOSE) {
            handleClose(alertId, username);
        } else if (actionType == ActionType.ACKNOWLEDGE) {
            handleAck(alertId, username);
        } else if (actionType == ActionType.UNACKNOWLEDGE) {
            handleUnAck(alertId, username);
        } else {
            log.info("Received unsupported action {}", actionType.toString());
        }

    }

    private void handleAck(final int alertId, final String userName) {
        updateStore(true, alertId);
        eventLog.log(PAGE_ACKNOWLEDGED, alertId, userName);
        log.info("User {} acknowledged alert #{}", userName, alertId);
    }

    private void handleUnAck(final int alertId, final String userName) {
        updateStore(false, alertId);
        eventLog.log(PAGE_UNACKNOWLEDGED, alertId, userName);
        log.info("User {} unacknowledged alert #{}", userName, alertId);
    }

    private void handleCreate(final int alertId, final List<String> recipients) {
        eventLog.log(PAGE_TRIGGERED, alertId, recipients.toString());
        log.info("Alert #{} assigned to {}", alertId, recipients.toString());
    }

    private void handleClose(final int alertId, final String userName) {
        updateStore(false, alertId);
        eventLog.log(PAGE_RESOLVED, alertId, userName);
        log.info("Alert #{} resolved by {}", alertId, userName);
    }

    private void updateStore(boolean ack, int alertId) {
        if (!config.isDryRun()) {
            if (ack) {
                alertStore.ackAlert(alertId);
            } else {
                alertStore.unackAlert(alertId);
            }
        } else {
            log.info("Update store Ack:{} Alert:{}", ack, alertId);
        }
    }
}
