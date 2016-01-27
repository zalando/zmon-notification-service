package org.zalando.zmon.notifications.controller;

import java.util.Optional;

import org.zalando.zmon.notifications.oauth.TokenInfoService;

public class DummyTokenInfoService implements TokenInfoService {

    @Override
    public Optional<String> lookupUid(String authorizationHeaderValue) {
        if ("Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308".equals(authorizationHeaderValue)) {
            return Optional.of("a-uid");
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "dummy-token-service";
    }
}
