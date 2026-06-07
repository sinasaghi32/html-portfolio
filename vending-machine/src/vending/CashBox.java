package vending;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tracks coins/bills inside the machine and calculates change. */
public class CashBox {
    private final LinkedHashMap<Integer, Integer> counts = new LinkedHashMap<>();
    private static final int[] DENOMS = {1000, 500, 100, 50, 10};

    public CashBox() {
        for (int denom : DENOMS) counts.put(denom, 10); // Constructor initializes default change inventory.
    }

    public synchronized boolean canAccept(int denom, int billTotal, int insertedTotal) {
        return counts.containsKey(denom) && billTotal <= 5000 && insertedTotal <= 7000;
    }

    public synchronized void add(int denom) { counts.put(denom, counts.getOrDefault(denom, 0) + 1); }

    public synchronized Map<Integer, Integer> makeChange(int amount) {
        LinkedHashMap<Integer, Integer> change = new LinkedHashMap<>();
        int remaining = amount;
        for (int denom : DENOMS) {
            int needed = Math.min(remaining / denom, counts.getOrDefault(denom, 0));
            if (needed > 0) {
                change.put(denom, needed);
                remaining -= denom * needed;
            }
        }
        if (remaining != 0) return null;
        for (Map.Entry<Integer, Integer> e : change.entrySet()) counts.put(e.getKey(), counts.get(e.getKey()) - e.getValue());
        return change;
    }

    public synchronized boolean hasChangeFor(int amount) { return makeChangeDryRun(amount) != null; }

    private Map<Integer, Integer> makeChangeDryRun(int amount) {
        LinkedHashMap<Integer, Integer> change = new LinkedHashMap<>();
        int remaining = amount;
        for (int denom : DENOMS) {
            int needed = Math.min(remaining / denom, counts.getOrDefault(denom, 0));
            if (needed > 0) { change.put(denom, needed); remaining -= denom * needed; }
        }
        return remaining == 0 ? change : null;
    }

    public synchronized int total() { int total = 0; for (var e : counts.entrySet()) total += e.getKey() * e.getValue(); return total; }

    public synchronized int collectKeepingMinimum() {
        int collected = 0;
        for (int denom : DENOMS) {
            int keep = denom <= 500 ? 5 : 1; // Minimum money reserved for future refunds/change.
            int count = counts.getOrDefault(denom, 0);
            if (count > keep) {
                collected += (count - keep) * denom;
                counts.put(denom, keep);
            }
        }
        return collected;
    }

    public synchronized String status() {
        StringBuilder sb = new StringBuilder();
        for (var e : counts.entrySet()) sb.append(e.getKey()).append("원 x ").append(e.getValue()).append("\n");
        sb.append("총액: ").append(total()).append("원");
        return sb.toString();
    }
}
