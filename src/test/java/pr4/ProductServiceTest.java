package pr4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr4.db.SqliteProductRepository;
import pr4.filter.ProductFilter;
import pr4.model.Product;
import pr4.service.ProductService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setup() {
        service = new ProductService(new SqliteProductRepository(":memory:"));
    }

    @Test
    void shouldCreateReadUpdateDelete() {
        int id = service.create(new Product("Apple", "Fruit", "FamilyFarm", 100, 1.5));

        assertThat(service.read(id)).isPresent();

        Product updated = service.read(id).get();
        updated.setQuantity(80);
        assertThat(service.update(updated)).isTrue();
        assertThat(service.read(id).get().getQuantity()).isEqualTo(80);

        assertThat(service.delete(id)).isTrue();
        assertThat(service.read(id)).isEmpty();
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> service.create(new Product("  ", "Fruit", "HayDayFarm", 1, 1.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> service.create(new Product("Apple", "Fruit", "HalytskaFarm", 1, -1.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectUpdateWithoutId() {
        assertThatThrownBy(() -> service.update(new Product("Tomato", "Vegetable", "JustFarm", 1, 1.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldSearchWithDynamicFilter() {
        service.create(new Product("Apple", "Fruit", "Pole", 100, 1.5));
        service.create(new Product("Banana", "Fruit", "Farm", 50, 0.8));
        service.create(new Product("Desk", "Furniture", "ForestAndCo", 10, 120.0));

        ProductFilter filter = new ProductFilter();
        filter.minPrice = 1.0;

        assertThat(service.search(filter, null))
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Apple", "Desk");
        assertThat(service.count(filter)).isEqualTo(2);
    }
}
