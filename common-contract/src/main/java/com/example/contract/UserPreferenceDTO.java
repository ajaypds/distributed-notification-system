package com.example.contract;

public record UserPreferenceDTO(
        String userId,
        boolean emailEnabled,
        boolean smsEnabled,
        boolean pushEnabled
) {}