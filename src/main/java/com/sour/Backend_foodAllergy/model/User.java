package com.sour.Backend_foodAllergy.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter@Setter
@Document(collection = "users")
public class User {
    @Id

    private Long id;

    private String name;
    private String email;
    private List<String> allergies;

    // Getters and Setters are handled by Lombok


}