package org.zalando.zmon.notifications.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.zalando.zmon.notifications.data.PublishNotificationPart;
import org.zalando.zmon.notifications.data.PublishRequestBody;
import org.zalando.zmon.notifications.push.JsonHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by hman on 16.12.15.
 */
public class JsonHelperTest {

    @Test
    public void jsonEntityForNotification() throws Exception {
        PublishRequestBody publishRequestBody = new PublishRequestBody();

        ObjectMapper objectMapper = new ObjectMapper();

        publishRequestBody.alert_id = 42;
        publishRequestBody.entity_id = "customer4.db.zalando";
        publishRequestBody.notification = new PublishNotificationPart("No database connection to master","Connection refused", "");
        StringEntity stringEntity = JsonHelper.jsonEntityFor("device-id-1", publishRequestBody);
        assertEquals(
                "{\"to\":\"device-id-1\",\"data\":{\"alert_id\":42,\"entity_id\":\"customer4.db.zalando\"},\"notification\":{\"title\":\"No database connection to master\",\"icon\":\"\",\"body\":\"Connection refused\"}}",
                stringEntityContent(stringEntity)
        );

        assertEquals("application/json", stringEntity.getContentType().getValue());

    }

    private String stringEntityContent(StringEntity stringEntity) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        stringEntity.writeTo(byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }
}
