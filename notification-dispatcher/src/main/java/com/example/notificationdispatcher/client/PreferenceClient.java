package com.example.notificationdispatcher.client;

import com.example.contract.UserPreferenceDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class PreferenceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public UserPreferenceDTO fetch(String userId) throws HttpServerErrorException {
        return restTemplate.getForObject(
                "http://user-preferences-service:8081/api/preferences/" + userId,
                UserPreferenceDTO.class
        );
    }
}
