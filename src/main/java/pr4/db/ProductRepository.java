package pr4.db;

import pr4.filter.Page;
import pr4.filter.ProductFilter;
import pr4.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    int insert(Product product);

    Optional<Product> getById(int id);

    boolean update(Product product);

    boolean delete(int id);

    List<Product> search(ProductFilter filter, Page page);

    int count(ProductFilter filter);

    int deleteAll();

    boolean getByName(String name);
}
