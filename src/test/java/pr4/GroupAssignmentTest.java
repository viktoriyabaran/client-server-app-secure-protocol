package pr4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr4.db.SqliteConnections;
import pr4.db.SqliteGroupRepository;
import pr4.db.SqliteProductRepository;
import pr4.filter.ProductFilter;
import pr4.model.Group;
import pr4.model.Product;
import pr4.service.GroupService;
import pr4.service.ProductService;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

class GroupAssignmentTest {

    private ProductService products;
    private GroupService groups;

    @BeforeEach
    void setup() {
        Connection connection = SqliteConnections.open(":memory:");
        products = new ProductService(new SqliteProductRepository(connection));
        groups = new GroupService(new SqliteGroupRepository(connection));
    }

    @Test
    void newProductHasNoGroup() {
        int productId = products.create(new Product("Apple", "Fruit", "HayDayFarm", 100, 1.5));

        assertThat(products.read(productId).get().getGroupId()).isNull();
    }

    @Test
    void shouldAssignProductToGroup() {
        int groupId = groups.create(new Group("Fruits"));
        int productId = products.create(new Product("Apple", "Fruit", "HayDayFarm", 100, 1.5));

        Product product = products.read(productId).get();
        product.setGroupId(groupId);
        products.update(product);

        assertThat(products.read(productId).get().getGroupId()).isEqualTo(groupId);
    }

    @Test
    void shouldReadGroupById() {
        int groupId = groups.create(new Group("Fruits"));

        assertThat(groups.read(groupId))
                .get()
                .isEqualTo(new Group(groupId, "Fruits"));
    }

    @Test
    void shouldFilterProductsByGroupName() {
        int fruits = groups.create(new Group("Fruits"));
        int tools = groups.create(new Group("Tools"));

        assignToGroup(products.create(new Product("Apple", "Fruit", "HayDayFarm", 100, 1.5)), fruits);
        assignToGroup(products.create(new Product("Hammer", "Tool", "Bosch", 5, 20.0)), tools);
        products.create(new Product("Orphan", "Misc", "None", 1, 1.0)); // no group here

        ProductFilter filter = new ProductFilter();
        filter.groupName = "Fruits";

        assertThat(products.search(filter, null))
                .extracting(Product::getName)
                .containsExactly("Apple");
        assertThat(products.count(filter)).isEqualTo(1);
    }

    private void assignToGroup(int productId, int groupId) {
        Product product = products.read(productId).get();
        product.setGroupId(groupId);
        products.update(product);
    }
}
