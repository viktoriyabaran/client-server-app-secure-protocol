package pr4.service;

import pr4.db.ProductRepository;
import pr4.filter.Page;
import pr4.filter.ProductFilter;
import pr4.model.Product;

import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductRepository db;

    public ProductService(ProductRepository db) {
        this.db = db;
    }

    public int create(Product product) {
        validate(product);
        return db.insert(product);
    }

    public Optional<Product> read(int id) {
        return db.getById(id);
    }

    public boolean update(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("Can't update a product without id");
        }
        validate(product);
        return db.update(product);
    }

    public boolean delete(int id) {
        return db.delete(id);
    }

    public List<Product> search(ProductFilter filter, Page page) {
        return db.search(filter, page);
    }

    public int count(ProductFilter filter) {
        return db.count(filter);
    }

    private void validate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product can't be null");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name can't be blank");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Product category can't be blank");
        }
        if (product.getManufacturer() == null || product.getManufacturer().isBlank()) {
            throw new IllegalArgumentException("Product manufacturer can't be blank");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity should be >= 0");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price should be >= 0");
        }
    }
}
