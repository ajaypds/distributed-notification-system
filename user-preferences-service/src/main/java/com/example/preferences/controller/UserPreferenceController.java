package com.example.preferences.controller;

import com.example.preferences.model.UserPreference;
import com.example.preferences.service.UserPreferenceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@AllArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService service;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPreference> get(@PathVariable String userId){
        UserPreference preference = service.getPreferences(userId);
        return ResponseEntity.ok(preference);
    }

    @PostMapping
    public ResponseEntity<UserPreference> save(@RequestBody UserPreference preference){
        return ResponseEntity.ok(service.save(preference));
    }
}
