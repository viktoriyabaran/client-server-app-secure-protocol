package pr4.model;

import java.util.Objects;

public class Product {

    private Integer id;
    private String name;
    private String category;
    private String manufacturer;
    private int quantity;
    private double price;

    public Product(String name, String category, String manufacturer, int quantity, double price) {
        this(null, name, category, manufacturer, quantity, price);
    }

    public Product(Integer id, String name, String category, String manufacturer, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return quantity == product.quantity
            && Double.compare(price, product.price) == 0
            && Objects.equals(id, product.id)
            && Objects.equals(name, product.name)
            && Objects.equals(category, product.category)
            && Objects.equals(manufacturer, product.manufacturer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, manufacturer, quantity, price);
    }

    @Override
    public String toString() {
        return "Product{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", category='" + category + '\'' +
            ", manufacturer='" + manufacturer + '\'' +
            ", quantity=" + quantity +
            ", price=" + price +
            '}';
    }
}
