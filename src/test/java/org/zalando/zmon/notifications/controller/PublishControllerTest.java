package org.zalando.zmon.notifications.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.store.InMemoryNotificationStore;
import org.zalando.zmon.notifications.store.NotificationStore;
import org.zalando.zmon.notifications.store.PreSharedKeyStore;

public class PublishControllerTest {

    private final int ALERT = 142;

    private MockMvc mockMvc;

    private TokenInfoService tokenInfoService;

    private NotificationStore notificationStore;

    private PushNotificationService notificationService;

    private PreSharedKeyStore keyStore;

    @Before
    public void setUp() {

        tokenInfoService = new DummyTokenInfoService();

        notificationStore = new InMemoryNotificationStore();
        notificationStore.addAlertForUid(ALERT, "6f18fb92-dbe3-41ac-ab8e-82be7f30e246");
        notificationStore.addDeviceForUid("6f18fb92-dbe3-41ac-ab8e-82be7f30e246",
                "6f18fb92-dbe3-41ac-ab8e-82be7f30e246");

        notificationService = new StubPushNotificationService();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new PublishController(tokenInfoService, notificationService, notificationStore, keyStore))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    public void publish() throws Exception {
        mockMvc.perform(post("/api/v1/publish").header("Authorization", "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{" + "\"alert_id\" : " + ALERT + "," + "\"entity_id\":\"customer5.db.zalando\","
                        + "\"notification\": {\"title\":\"No database connection to master\",\"body\":\"\",\"icon\":\"\"}"
                        + "}"))
                .andExpect(status().isOk());

        assertEquals(
                "stub-pushed-notifications: {6f18fb92-dbe3-41ac-ab8e-82be7f30e246=[PublishRequestBody{alert_id=" + ALERT
                        + ", notification=PublishNotificationPart{title=No database connection to master, body=, icon=}, entity_id=customer5.db.zalando}]}",
                ((StubPushNotificationService) notificationService).toString());
    }

}
