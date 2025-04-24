package com.sour.Backend_foodAllergy.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Document(collection = "users")
public class User {

    @Id
    private Long id;
    private String name;
    private String email;
    private List<String> allergies;

    // Getters and Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }
}
