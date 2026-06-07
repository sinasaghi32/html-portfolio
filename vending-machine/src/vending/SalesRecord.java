package vending;

import java.time.LocalDate;

/** Immutable sales row persisted by the local file-backed database. */
public record SalesRecord(LocalDate date, int beverageId, String beverageName, int quantity, int amount, String machineId) {
    public String toCsv() {
        return date + "," + beverageId + "," + beverageName.replace(",", " ") + "," + quantity + "," + amount + "," + machineId;
    }

    public static SalesRecord fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new SalesRecord(LocalDate.parse(p[0]), Integer.parseInt(p[1]), p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]), p.length > 5 ? p[5] : "VM-001");
    }
}
