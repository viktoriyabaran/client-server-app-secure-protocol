package pr4;

import pr4.db.ProductRepository;
import pr4.db.SqliteProductRepository;
import pr4.filter.Page;
import pr4.filter.ProductFilter;
import pr4.model.Product;
import pr4.service.ProductService;

public class Main {

    public static void main(String[] args) {
        ProductRepository db = new SqliteProductRepository("warehouse.db");
        ProductService service = new ProductService(db);

        db.deleteAll();

        service.create(new Product("Apple", "Fruit", "Bazar", 100, 1.5));
        service.create(new Product("Banana", "Fruit", "Bazar", 50, 0.8));
        int carrotId = service.create(new Product("Carrot", "Vegetable", "Silpo", 200, 0.5));
        service.create(new Product("Desk", "Furniture", "IKEA", 10, 120.0));

        System.out.println("Total products: " + service.count(new ProductFilter()));

        ProductFilter filter = new ProductFilter();
        filter.category = "Fruit";
        filter.minPrice = 1.0;
        System.out.println("Fruit with price over 1.0: " + service.search(filter, Page.of(1, 10)));

        System.out.println("Carrot: " + service.read(carrotId));
        service.read(carrotId).ifPresent(p -> {
            p.setQuantity(150);
            service.update(p);
        });
        System.out.println("Carrot after update: " + service.read(carrotId));
        service.delete(carrotId);
        System.out.println("Carrot after delete: " + service.read(carrotId));
    }
}
