package com.sour.Backend_foodAllergy.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AllergyService {
    // Liste d'allergènes étendue et standardisée
    private static final Set<String> KNOWN_ALLERGENS = Set.of(
            "Lait", "Arachide", "Œuf", "Gluten", "Soja", "Noisette",
            "Sésame", "Amande", "Noix", "Noix de cajou", "Pistache",
            "Noix de macadamia", "Noix du Brésil", "Pécan", "Blé", "Crustacés",
            "Crevette", "Crabe", "Homard", "Poisson", "Moutarde", "Céleri",
            "Lupin", "Sulfite", "Mollusque", "Kiwi", "Banane", "Avocat"
    );

    // Détection améliorée par IA/NLP
    public List<String> detectAllergens(String text, String userAllergies) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        // Normalisation du texte pour la recherche
        String normalizedText = text.toLowerCase()
                .replaceAll("[^a-z\\s]", " ") // Garde uniquement les lettres et espaces
                .replaceAll("\\s+", " ");     // Normalise les espaces

        // Traitement des allergies de l'utilisateur
        Set<String> userAllergySet = Arrays.stream(userAllergies
                        .toLowerCase()
                        .replaceAll("[\\[\\]\"]", "") // Supprime les caractères de tableau JSON
                        .split("\\s*,\\s*"))          // Sépare sur les virgules avec espaces optionnels
                .map(allergene -> allergene.replaceAll("[^a-z]", "")) // Nettoie chaque allergène
                .filter(allergene -> !allergene.isEmpty())
                .collect(Collectors.toSet());

        // Création de motifs de recherche pour chaque allergène
        return userAllergySet.stream()
                .filter(allergene -> {
                    // Recherche exacte de mot entier
                    Pattern pattern = Pattern.compile("\\b" + allergene + "\\b");
                    return pattern.matcher(normalizedText).find();
                })
                .collect(Collectors.toList());
    }

    // Évaluation améliorée du risque avec niveaux de sévérité
    public String estimateRisk(List<String> matchedAllergens) {
        if (matchedAllergens.isEmpty()) {
            return "Aucun";
        }

        // Définition des niveaux de sévérité pour différents allergènes
        Map<String, Integer> severityMap = Map.of(
                "arachide", 3, "noix", 3, "crustacés", 3,
                "lait", 1, "œuf", 1, "soja", 1, "blé", 1
        );

        int riskScore = matchedAllergens.stream()
                .mapToInt(allergene -> severityMap.getOrDefault(allergene.toLowerCase(), 1))
                .sum();

        if (riskScore >= 5) return "Élevé";
        if (riskScore >= 3) return "Modéré";
        return "Faible";
    }

    // Obtention des allergènes par catégories
    public Map<String, List<String>> getCategorizedAllergens() {
        return Map.of(
                "Produits laitiers", List.of("Lait"),
                "Noix", List.of("Arachide", "Amande", "Noisette", "Noix", "Noix de cajou"),
                "Céréales", List.of("Blé", "Gluten"),
                "Fruits de mer", List.of("Poisson", "Crustacés", "Crevette", "Crabe"),
                "Autres", List.of("Œuf", "Soja", "Sésame", "Moutarde")
        );
    }

    // Méthodes utilitaires
    private String preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "") // Supprime la ponctuation
                .replaceAll("\\s+", " ");       // Normalise les espaces
    }

    private boolean containsAllergen(String text, String allergene) {
        // Recherche exacte de base
        if (text.contains(allergene)) {
            return true;
        }

        // Gestion des formes plurielles et variations courantes
        if (allergene.endsWith("y")) {
            String plural = allergene.substring(0, allergene.length()-1) + "ies";
            if (text.contains(plural)) return true;
        } else {
            if (text.contains(allergene + "s")) return true;
        }

        // Possibilité d'ajouter une recherche plus sophistiquée (intégration avec une bibliothèque NLP)
        return false;
    }

    public List<String> getCommonAllergens() {
        return List.of(
                // Allergènes majeurs (les plus courants)
                "Lait", "Œufs", "Arachides",
                "Noix", "Soja", "Blé",
                "Poisson", "Crustacés", "Sésame",

                // Autres allergènes courants
                "Gluten", "Moutarde", "Céleri",
                "Lupin", "Sulfites", "Mollusques",

                // Noix spécifiques
                "Amandes", "Noisettes", "Noix",
                "Noix de cajou", "Pécans", "Pistaches",
                "Noix de macadamia", "Noix du Brésil",

                // Crustacés spécifiques
                "Crevettes", "Crabes", "Homards",
                "Écrevisses", "Palourdes", "Moules",
                "Huîtres", "Coquilles Saint-Jacques",

                // Intolérances émergentes/courantes
                "Maïs", "Ail", "Oignon",
                "Kiwi", "Banane", "Avocat"
        );
    }

    public List<Allergen> getCommonAllergensWithMetadata() {
        return List.of(
                new Allergen("Lait", "Produits laitiers", 0.65, "Élevé"),
                new Allergen("Arachides", "Légumineuses", 0.50, "Élevé"),
                new Allergen("Crustacés", "Fruits de mer", 0.40, "Élevé"),
                new Allergen("Noix", "Noix", 0.30, "Élevé"),
                new Allergen("Œufs", "Produit animal", 0.25, "Modéré"),
                new Allergen("Blé", "Céréale", 0.20, "Modéré"),
                new Allergen("Soja", "Légumineuse", 0.15, "Modéré"),
                new Allergen("Poisson", "Fruits de mer", 0.10, "Modéré")
        );
    }

    // Enregistrement support (Java 16+) ou classe
    public record Allergen(
            String nom,
            String categorie,
            double prevalence,  // Échelle 0.0-1.0
            String severiteTypique
    ) {}
}