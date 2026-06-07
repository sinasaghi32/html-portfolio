package vending;

import java.time.LocalDate;

/** Represents one drink slot in the vending machine. */
public class Beverage {
    private final int id;
    private String name;
    private int price;
    private int stock;
    private LocalDate soldOutDate;

    public Beverage(int id, String name, int price, int stock) {
        if (price <= 0) throw new IllegalArgumentException("Price must be positive.");
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.soldOutDate = stock == 0 ? LocalDate.now() : null;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public LocalDate getSoldOutDate() { return soldOutDate; }
    public boolean isSoldOut() { return stock <= 0; }

    public void rename(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }

    public void changePrice(int price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be positive.");
        this.price = price;
    }

    public void restock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Restock amount must be positive.");
        this.stock += amount;
        this.soldOutDate = null;
    }

    public void vendOne() {
        if (stock <= 0) throw new IllegalStateException(name + " is sold out.");
        stock--;
        if (stock == 0) soldOutDate = LocalDate.now();
    }

    public String toCsv() {
        return id + "," + escape(name) + "," + price + "," + stock + "," + (soldOutDate == null ? "" : soldOutDate);
    }

    public static Beverage fromCsv(String line) {
        String[] parts = line.split(",", -1);
        Beverage beverage = new Beverage(Integer.parseInt(parts[0]), unescape(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        if (parts.length > 4 && !parts[4].isBlank()) beverage.soldOutDate = LocalDate.parse(parts[4]);
        return beverage;
    }

    private static String escape(String value) { return value.replace("%", "%25").replace(",", "%2C"); }
    private static String unescape(String value) { return value.replace("%2C", ",").replace("%25", "%"); }
}
