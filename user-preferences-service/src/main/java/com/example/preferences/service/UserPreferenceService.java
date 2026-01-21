package com.example.preferences.service;

import com.example.preferences.model.UserPreference;
import com.example.preferences.repository.UserPreferenceRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository repository;

    @Cacheable(value = "user-preferences", key = "#userId")
    public UserPreference getPreferences(String userId){
        return repository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
    }

    @Cacheable(value = "user-preferences", key="#preference.userId")
    public UserPreference save(UserPreference preference){
        return repository.save(preference);
    }

}
