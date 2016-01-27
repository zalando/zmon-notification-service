package org.zalando.zmon.notifications.push;

import static com.google.common.base.Charsets.UTF_8;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.zalando.zmon.notifications.data.PublishRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper(); // Setup
                                                                   // Jackson

    static StringEntity jsonEntityFor(String deviceToken, PublishRequestBody notification)
            throws JsonProcessingException {
        ObjectNode request = mapper.createObjectNode();

        request.put("to", deviceToken);

        ObjectNode data = request.putObject("data");
        data.put("alert_id", notification.alert_id);
        data.put("entity_id", notification.entity_id);

        ObjectNode notify = request.putObject("notification");
        notify.put("title", notification.notification.title);
        notify.put("icon", notification.notification.icon);
        notify.put("body", notification.notification.body);

        String json = mapper.writeValueAsString(request);
        StringEntity result = new StringEntity(json, UTF_8);
        result.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        return result;
    }
}
