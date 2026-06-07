package model;

/** 음료 1개의 정보를 저장한다. id/name/price/stock/soldCount는 보고서 설명용 핵심 필드이다. */
public class Drink {
    private int id;
    private String name;
    private int price;
    private int stock;
    private int soldCount;

    public Drink(int id, String name, int price, int stock, int soldCount) {
        this.id = id; this.name = name; this.price = price; this.stock = stock; this.soldCount = soldCount;
    }
    public int getId(){ return id; }
    public String getName(){ return name; }
    public int getPrice(){ return price; }
    public int getStock(){ return stock; }
    public int getSoldCount(){ return soldCount; }
    public boolean isSoldOut(){ return stock <= 0; }
    public void setName(String name){ this.name = name; }
    public void setPrice(int price){ this.price = price; }
    public void addStock(int amount){ this.stock += amount; }
    public void decreaseStock(){ if (stock > 0) stock--; }
    public void increaseSoldCount(){ soldCount++; }
    public String toCsv(){ return id + "," + name + "," + price + "," + stock + "," + soldCount; }
    public static Drink fromCsv(String line){ String[] p=line.split(",",-1); return new Drink(Integer.parseInt(p[0]),p[1],Integer.parseInt(p[2]),Integer.parseInt(p[3]),Integer.parseInt(p[4])); }
    @Override public String toString(){ return id + ". " + name + " / " + price + "원 / 재고 " + stock + " / 판매 " + soldCount; }
}
