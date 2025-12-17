package com.paletlabels.service;

import com.paletlabels.model.Product;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductRepository repository;
    private List<Product> cachedProducts;

    public ProductService() {
        this.repository = new ProductRepository(Paths.get(System.getProperty("user.home"), ".palet-labels", "products.json"));
        this.cachedProducts = new ArrayList<>(repository.load());
    }

    public List<Product> getAll() {
        return Collections.unmodifiableList(cachedProducts);
    }

    public void add(Product product) {
        cachedProducts.add(product);
        repository.save(cachedProducts);
    }

    public void update(int index, Product updated) {
        cachedProducts.set(index, updated);
        repository.save(cachedProducts);
    }

    public void delete(int index) {
        cachedProducts.remove(index);
        repository.save(cachedProducts);
    }

    public Optional<Product> findByName(String name) {
        return cachedProducts.stream().filter(p -> p.getName().equals(name)).findFirst();
    }
}
