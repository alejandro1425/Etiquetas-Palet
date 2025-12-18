package com.paletlabels.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
    private String name;
    private int unitsPerBox;
    private double weightPerUnitKg;
    private String ean13;
    private boolean variableWeight; // true = (02), false = (01)

    public Product() {
    }

    public Product(String name, int unitsPerBox, double weightPerUnitKg, String ean13) {
        this.name = name;
        this.unitsPerBox = unitsPerBox;
        this.weightPerUnitKg = weightPerUnitKg;
        this.ean13 = ean13;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("units_per_box")
    public int getUnitsPerBox() {
        return unitsPerBox;
    }

    public void setUnitsPerBox(int unitsPerBox) {
        this.unitsPerBox = unitsPerBox;
    }

    @JsonProperty("weight_per_unit_kg")
    public double getWeightPerUnitKg() {
        return weightPerUnitKg;
    }

    public void setWeightPerUnitKg(double weightPerUnitKg) {
        this.weightPerUnitKg = weightPerUnitKg;
    }

    public String getEan13() {
        return ean13;
    }

    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    @JsonIgnore
    public double calculateNetWeightKg(int boxes) {
        return boxes * unitsPerBox * weightPerUnitKg;
    }

    @Override
    public String toString() {
        return name;
    }

    // Peso fijo/variable
    // Se persiste como "variable_weight" en products.json
    @JsonProperty("variable_weight")
    public boolean isVariableWeight() {
        return variableWeight;
    }

    @JsonProperty("variable_weight")
    public void setVariableWeight(boolean variableWeight) {
        this.variableWeight = variableWeight;
    }

    public String getGtinAi() {
        return variableWeight ? "02" : "01";
    }
}
