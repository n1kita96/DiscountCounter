/**
 * @author Mykyta Shvets
 */
public class Product {
    private String name;
    private double price;
    private String collection;

    public Product(String name, double price, String collection) {
        this.name = name;
        this.price = price;
        this.collection = collection;
    }

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", collection='" + collection + '\'' +
                '}';
    }
}
