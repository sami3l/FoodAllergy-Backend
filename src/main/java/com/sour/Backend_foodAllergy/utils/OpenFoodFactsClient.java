package com.sour.Backend_foodAllergy.utils;

import com.sour.Backend_foodAllergy.dto.ProductInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenFoodFactsClient {

    private final RestTemplate restTemplate;

    public OpenFoodFactsClient() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "FoodAllergyAI - Android App (sami@example.com)");
            return execution.execute(request, body);
        });
    }

    public String getIngredientsFromBarcode(String barcode) {
        try {
            String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || body.get("status").equals(0)) return null;

            Map<String, Object> product = (Map<String, Object>) body.get("product");
            return (String) product.get("ingredients_text");
        } catch (Exception e) {
            return null;
        }
    }

    public ProductInfoResponse getProductInfo(String barcode) {
        try {
            String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || Integer.valueOf(0).equals(body.get("status"))) return null;

            Map<String, Object> product = (Map<String, Object>) body.get("product");

            ProductInfoResponse productInfo = new ProductInfoResponse();
            productInfo.setBarcode(barcode);
            productInfo.setProductName((String) product.get("product_name"));
            productInfo.setIngredients((String) product.get("ingredients_text"));
            productInfo.setImageUrl((String) product.get("image_url"));
            productInfo.setBrand((String) product.get("brands"));
            productInfo.setNutriscoreGrade((String) product.get("nutriscore_grade"));
            productInfo.setServingSize((String) product.get("serving_size"));

            // Parse categories
            String categoriesStr = (String) product.get("categories");
            if (categoriesStr != null && !categoriesStr.isEmpty()) {
                List<String> categories = new ArrayList<>();
                for (String category : categoriesStr.split(",")) {
                    categories.add(category.trim());
                }
                productInfo.setCategories(categories);
            }

            // Parse allergens
            String allergensStr = (String) product.get("ingredients_text");
            if (allergensStr != null && !allergensStr.isEmpty()) {
                List<String> allergens = new ArrayList<>();
                for (String allergen : allergensStr.replace("en:", "").split(",")) {
                    allergens.add(allergen.trim());
                }
                productInfo.setAllergens(allergens);
            }

            return productInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}