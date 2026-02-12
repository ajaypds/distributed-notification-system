package com.example.notificationdispatcher.client;

import com.example.contract.UserPreferenceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreferenceClient {

    private final RestTemplate restTemplate; //Bean for this is defined in OpenTelemetryConfig

    public UserPreferenceDTO fetch(String userId) throws HttpServerErrorException {
        log.info("RestTemplate instance in PreferenceClient: " + restTemplate);
        return restTemplate.getForObject(
                "http://user-preferences-service:8081/api/preferences/" + userId,
                UserPreferenceDTO.class
        );
    }
}
