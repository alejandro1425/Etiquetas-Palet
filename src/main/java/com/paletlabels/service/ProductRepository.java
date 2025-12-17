package com.paletlabels.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paletlabels.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private final Path storagePath;
    private final ObjectMapper mapper;

    public ProductRepository(Path storagePath) {
        this.storagePath = storagePath;
        this.mapper = new ObjectMapper();
    }

    public List<Product> load() {
        if (!Files.exists(storagePath)) {
            return createDefaults();
        }
        try {
            return mapper.readValue(storagePath.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return createDefaults();
        }
    }

    public void save(List<Product> products) {
        try {
            Files.createDirectories(storagePath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), products);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar productos", e);
        }
    }

    private List<Product> createDefaults() {
        List<Product> defaults = new ArrayList<>();
        defaults.add(new Product("26590380- CHORIZO EXTRA DULCE 50% DUROC C/20", 20, 0.25, "26590380"));
        defaults.add(new Product("26590420- SALCHICHON EXTRA 50% DUROC C/15", 15, 0.22, "26590420"));
        save(defaults);
        return defaults;
    }
}
