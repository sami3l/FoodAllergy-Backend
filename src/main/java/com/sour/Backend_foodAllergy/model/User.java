package com.sour.Backend_foodAllergy.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;
    private String name;
    private String email;
    private List<String> allergies;

    private Role role; // Utilise maintenant ton propre enum

    public void clearSensitiveData() {
        this.password = null;
    }
}
