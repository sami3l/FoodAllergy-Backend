package com.sour.Backend_foodAllergy.dto;

import java.util.List;

public class AllergyUpdateRequest {
    private List<String> allergies;

    // Default constructor
    public AllergyUpdateRequest() {
    }

    // Constructor with parameters
    public AllergyUpdateRequest(List<String> allergies) {
        this.allergies = allergies;
    }

    // Getters and setters
    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    @Override
    public String toString() {
        return "AllergyUpdateRequest{" +
                "allergies=" + allergies +
                '}';
    }
}