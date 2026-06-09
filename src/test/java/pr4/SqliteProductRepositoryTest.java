package pr4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr4.db.ProductRepository;
import pr4.db.SqliteProductRepository;
import pr4.filter.Page;
import pr4.filter.ProductFilter;
import pr4.model.Product;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteProductRepositoryTest {

    private ProductRepository db;

    @BeforeEach
    void setup() {
        db = new SqliteProductRepository(":memory:");
        db.insert(new Product("Apple", "Fruit", "HayDayFarm", 100, 1.5));
        db.insert(new Product("Pear", "Fruit", "HayDayFarm", 50, 0.8));
        db.insert(new Product("Carrot", "Vegetable", "VeganParadiseCo", 200, 0.5));
        db.insert(new Product("Shelf", "Furniture", "WoodSellers", 10, 120.0));
    }

    @Test
    void shouldInsertAndReadBackById() {
        int newId = db.insert(new Product("Eggplant", "Vegetable", "VeganParadiseCo", 30, 2.2));

        assertThat(db.getById(newId))
                .isPresent()
                .get()
                .isEqualTo(new Product(newId, "Eggplant", "Vegetable", "VeganParadiseCo", 30, 2.2));
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertThat(db.getById(999_999)).isEmpty();
    }

    @Test
    void shouldUpdateExistingProduct() {
        int id = db.insert(new Product("Egg", "Dairy", "DairyCo", 12, 3.0));

        boolean updated = db.update(new Product(id, "Eggs", "Dairy", "DairyCo", 24, 3.5));

        assertThat(updated).isTrue();
        assertThat(db.getById(id))
                .get()
                .isEqualTo(new Product(id, "Eggs", "Dairy", "DairyCo", 24, 3.5));
    }

    @Test
    void shouldReportFalseWhenUpdatingMissingProduct() {
        assertThat(db.update(new Product(999_999, "Ghost", "None", "None", 0, 0))).isFalse();
    }

    @Test
    void shouldDeleteById() {
        int id = db.insert(new Product("Temp", "Misc", "Acme", 1, 1.0));

        assertThat(db.delete(id)).isTrue();
        assertThat(db.getById(id)).isEmpty();
        assertThat(db.delete(id)).isFalse();
    }

    @Test
    void shouldSearchAllWhenFilterIsEmpty() {
        assertThat(db.search(new ProductFilter(), null)).hasSize(4);
    }

    @Test
    void shouldFilterByNameSubstring() {
        ProductFilter filter = new ProductFilter();
        filter.name = "rr";

        List<Product> found = db.search(filter, null);

        assertThat(found).extracting(Product::getName).containsExactly("Carrot");
    }

    @Test
    void shouldFilterByCategory() {
        ProductFilter filter = new ProductFilter();
        filter.category = "Fruit";

        assertThat(db.search(filter, null))
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Apple", "Pear");
    }

    @Test
    void shouldFilterByManufacturer() {
        ProductFilter filter = new ProductFilter();
        filter.manufacturer = "VeganParadiseCo";

        assertThat(db.search(filter, null))
                .extracting(Product::getName)
                .containsExactly("Carrot");
    }

    @Test
    void shouldFilterByQuantityRange() {
        ProductFilter filter = new ProductFilter();
        filter.minQuantity = 50;
        filter.maxQuantity = 100;

        assertThat(db.search(filter, null))
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Apple", "Pear");
    }

    @Test
    void shouldFilterByPriceGreaterThan() {
        ProductFilter filter = new ProductFilter();
        filter.minPrice = 3.0;

        assertThat(db.search(filter, null))
                .extracting(Product::getName)
                .containsExactly("Shelf");
    }

    @Test
    void shouldCombineNameAndCategoryFilters() {
        ProductFilter filter = new ProductFilter();
        filter.name = "a";
        filter.category = "Fruit";

        assertThat(db.search(filter, null))
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Apple", "Pear");
    }

    @Test
    void shouldPaginateResults() {
        ProductFilter all = new ProductFilter();

        List<Product> firstPage = db.search(all, new Page(2, 0));
        List<Product> secondPage = db.search(all, new Page(2, 2));

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(firstPage).doesNotContainAnyElementsOf(secondPage);
    }

    @Test
    void deleteAllShouldEmptyTheTable() {
        assertThat(db.deleteAll()).isEqualTo(4);
        assertThat(db.count(new ProductFilter())).isZero();
    }
}
