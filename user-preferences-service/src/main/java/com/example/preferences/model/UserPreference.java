package com.example.preferences.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPreference {

    @Id
    private String userId;

    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;

}
