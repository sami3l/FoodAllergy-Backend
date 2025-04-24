package com.sour.Backend_foodAllergy.dto;


import java.util.List;

public class ProductInfoResponse {
    private String productName;
    private String barcode;
    private String ingredients;
    private String imageUrl;
    private String brand;
    private List<String> categories;
    private String nutriscoreGrade;
    private List<String> allergens;
    private String servingSize;

    // Default constructor
    public ProductInfoResponse() {
    }

    // Constructor with essential parameters
    public ProductInfoResponse(String productName, String barcode, String ingredients) {
        this.productName = productName;
        this.barcode = barcode;
        this.ingredients = ingredients;
    }

    // Full constructor
    public ProductInfoResponse(String productName, String barcode, String ingredients,
                               String imageUrl, String brand, List<String> categories,
                               String nutriscoreGrade, List<String> allergens, String servingSize) {
        this.productName = productName;
        this.barcode = barcode;
        this.ingredients = ingredients;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.categories = categories;
        this.nutriscoreGrade = nutriscoreGrade;
        this.allergens = allergens;
        this.servingSize = servingSize;
    }

    // Getters and setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getNutriscoreGrade() {
        return nutriscoreGrade;
    }

    public void setNutriscoreGrade(String nutriscoreGrade) {
        this.nutriscoreGrade = nutriscoreGrade;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    @Override
    public String toString() {
        return "ProductInfoResponse{" +
                "productName='" + productName + '\'' +
                ", Image='" + getImageUrl() + '\'' +
                ", barcode='" + barcode + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", brand='" + brand + '\'' +
                ", nutriscoreGrade='" + nutriscoreGrade + '\'' +
                ", allergens=" + allergens +
                '}';
    }
}