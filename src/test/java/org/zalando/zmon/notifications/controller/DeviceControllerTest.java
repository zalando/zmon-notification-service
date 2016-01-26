package org.zalando.zmon.notifications.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.zmon.notifications.oauth.DummyTokenInfoService;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.store.InMemoryNotificationStore;
import org.zalando.zmon.notifications.store.NotificationStore;

public class DeviceControllerTest {

    private final String DEVICE = UUID.fromString("6f18fb92-dbe3-41ac-ab8e-82be7f30e246").toString();

    private final int ALERT = 142;

    private MockMvc mockMvc;

    private TokenInfoService tokenInfoService;

    private NotificationStore notificationStore;

    @Before
    public void setUp() {

        tokenInfoService = new DummyTokenInfoService();
        notificationStore = new InMemoryNotificationStore();

        this.mockMvc = MockMvcBuilders.standaloneSetup(new DeviceController(tokenInfoService, notificationStore))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    public void unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/device").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"registration_token\" : \"" + DEVICE + "\" }")
                .header("Authorization", "Bearer 1334ff68-ba2e-4b07-8e67-9304c55f8308") // wrong
                                                                                        // token;
                                                                                        // see:
                                                                                        // DummyTokenInfoService
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void happyPath() throws Exception {
        // insert device
        mockMvc.perform(post("/api/v1/device").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"registration_token\" : \"" + DEVICE + "\" }")
                .header("Authorization", "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308")).andExpect(status().isOk());

        assertEquals("in-mem-store: devices={a-uid=[6f18fb92-dbe3-41ac-ab8e-82be7f30e246]} alerts={}",
                notificationStore.toString());

        // insert alert
        mockMvc.perform(post("/api/v1/subscription").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"alert_id\" : " + ALERT + " }")
                .header("Authorization", "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308")).andExpect(status().isOk());

        assertEquals("in-mem-store: devices={a-uid=[6f18fb92-dbe3-41ac-ab8e-82be7f30e246]} alerts={142=[a-uid]}",
                notificationStore.toString());

    }

}
