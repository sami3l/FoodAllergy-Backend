package com.sour.Backend_foodAllergy.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AllergyService {

    // Extended and standardized list of known allergens
    private static final Set<String> KNOWN_ALLERGENS = Set.of(
            "milk", "peanut", "egg", "gluten", "soy", "hazelnut",
            "sesame", "almond", "nut", "cashew", "pistachio",
            "macadamia", "brazil nut", "pecan", "wheat", "shellfish",
            "shrimp", "crab", "lobster", "fish", "mustard", "celery",
            "lupin", "sulfite", "mollusk", "kiwi", "banana", "avocado"
    );

    /**
     * Detects which of the user's allergies appear in a list of ingredients.
     */
    public List<String> detectAllergensInIngredients(
            List<String> ingredients,
            Set<String> userAllergySet
    ) {
        return ingredients.stream()
                .map(String::toLowerCase)
                .filter(userAllergySet::contains)
                .collect(Collectors.toList());
    }

    // Improved detection using AI/NLP
    public List<String> detectAllergens(String text, String userAllergies) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        // Normalize text for matching
        String normalizedText = text.toLowerCase()
                .replaceAll("[^a-z\\s]", " ") // Keep only letters and spaces
                .replaceAll("\\s+", " ");     // Normalize whitespace

        // Parse the user's allergy list into a clean set
        Set<String> userAllergySet = Arrays.stream(
                        userAllergies.toLowerCase()
                                .replaceAll("[\\[\\]\"]", "") // Remove JSON array brackets/quotes
                                .split("\\s*,\\s*"))           // Split on commas
                .map(allergen -> allergen.replaceAll("[^a-z]", ""))    // Remove non-letters
                .filter(allergen -> !allergen.isEmpty())
                .collect(Collectors.toSet());

        // For each allergen, check for whole-word matches
        return userAllergySet.stream()
                .filter(allergen -> {
                    Pattern pattern = Pattern.compile("\\b" + allergen + "\\b");
                    return pattern.matcher(normalizedText).find();
                })
                .collect(Collectors.toList());
    }

    // Enhanced risk estimation with severity levels
    public String estimateRisk(List<String> matchedAllergens) {
        if (matchedAllergens.isEmpty()) {
            return "None";
        }

        // Severity scores for specific allergens
        Map<String, Integer> severityMap = Map.of(
                "peanut", 3, "nut", 3, "shellfish", 3,
                "milk", 1, "egg", 1, "soy", 1, "wheat", 1
        );

        int riskScore = matchedAllergens.stream()
                .mapToInt(allergen -> severityMap.getOrDefault(allergen.toLowerCase(), 1))
                .sum();

        if (riskScore >= 5) return "High";
        if (riskScore >= 3) return "Moderate";
        return "Low";
    }

    /**
     * Returns allergens grouped by category.
     */
    public Map<String, List<String>> getCategorizedAllergens() {
        return Map.of(
                "Dairy Products", List.of("milk"),
                "Nuts",           List.of("peanut", "almond", "hazelnut", "nut", "cashew"),
                "Cereals",        List.of("wheat", "gluten"),
                "Seafood",        List.of("fish", "shellfish", "shrimp", "crab"),
                "Others",         List.of("egg", "soy", "sesame", "mustard")
        );
    }

    // Utility methods

    /** Simple text preprocessor (remove punctuation + normalize spaces) */
    private String preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\s+", " ");
    }

    /**
     * Basic allergen containment check with simple plural handling.
     */
    private boolean containsAllergen(String text, String allergen) {
        if (text.contains(allergen)) {
            return true;
        }
        // Handle common plural forms
        if (allergen.endsWith("y")) {
            String plural = allergen.substring(0, allergen.length() - 1) + "ies";
            if (text.contains(plural)) return true;
        } else {
            if (text.contains(allergen + "s")) return true;
        }
        return false;
    }

    public List<String> getCommonAllergens() {
        return List.of(
                // Major/common allergens
                "Milk", "Eggs", "Peanuts",
                "Nuts", "Soy", "Wheat",
                "Fish", "Shellfish", "Sesame",

                // Other common allergens
                "Gluten", "Mustard", "Celery",
                "Lupin", "Sulfites", "Mollusks",

                // Specific nuts
                "Almonds", "Hazelnuts", "Nuts",
                "Cashews", "Pecans", "Pistachios",
                "Macadamia Nuts", "Brazil Nuts",

                // Specific shellfish
                "Shrimp", "Crab", "Lobster",
                "Crawfish", "Clams", "Mussels",
                "Oysters", "Scallops",

                // Emerging/intolerance ingredients
                "Corn", "Garlic", "Onion",
                "Kiwi", "Banana", "Avocado"
        );
    }

    public List<Allergen> getCommonAllergensWithMetadata() {
        return List.of(
                new Allergen("Milk",      "Dairy Products", 0.65, "High"),
                new Allergen("Peanuts",   "Legumes",       0.50, "High"),
                new Allergen("Shellfish", "Seafood",       0.40, "High"),
                new Allergen("Nuts",      "Nuts",           0.30, "High"),
                new Allergen("Eggs",      "Animal Products",0.25, "Moderate"),
                new Allergen("Wheat",     "Cereals",        0.20, "Moderate"),
                new Allergen("Soy",       "Legumes",        0.15, "Moderate"),
                new Allergen("Fish",      "Seafood",        0.10, "Moderate")
        );
    }

    /**
     * Simple data class for allergen metadata.
     */
    public record Allergen(
            String name,
            String category,
            double prevalence,      // Scale: 0.0–1.0
            String typicalSeverity  // e.g. "High", "Moderate", "Low"
    ) {}
}
