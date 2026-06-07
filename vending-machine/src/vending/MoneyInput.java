package vending;

/** Dynamically created per transaction and nulled after sale/refund to model dynamic allocation release. */
public class MoneyInput {
    private int total;
    private int billTotal;

    public void add(int denom) {
        if (denom == 1000) billTotal += denom;
        total += denom;
    }

    public int getTotal() { return total; }
    public int getBillTotal() { return billTotal; }
}
