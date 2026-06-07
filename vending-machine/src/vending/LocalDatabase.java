package vending;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Simple file-backed database for manager data without third-party dependencies. */
public class LocalDatabase {
    private final Path dataDir;
    private final Path beverageFile;
    private final Path salesFile;
    private final Path adminFile;
    private final Path auditFile;

    public LocalDatabase(Path dataDir) {
        this.dataDir = dataDir;
        beverageFile = dataDir.resolve("beverages.csv");
        salesFile = dataDir.resolve("sales.csv");
        adminFile = dataDir.resolve("admin.txt");
        auditFile = dataDir.resolve("audit.log");
    }

    public void initialize() throws IOException {
        Files.createDirectories(dataDir);
        if (!Files.exists(beverageFile)) saveBeverages(defaultBeverages());
        if (!Files.exists(salesFile)) Files.writeString(salesFile, "date,beverageId,beverageName,quantity,amount,machineId\n2026-06-01,1,믹스커피,2,400,VM-001\n2026-06-01,4,캔커피,1,500,VM-001\n2026-06-02,7,탄산음료,3,2250,VM-001\n");
        if (!Files.exists(adminFile)) Files.writeString(adminFile, "Admin!123\n");
        if (!Files.exists(auditFile)) Files.writeString(auditFile, LocalDate.now() + " database initialized\n");
    }

    public CustomLinkedList<Beverage> loadBeverages() throws IOException {
        CustomLinkedList<Beverage> list = new CustomLinkedList<>();
        for (String line : Files.readAllLines(beverageFile)) if (!line.isBlank() && !line.startsWith("id,")) list.add(Beverage.fromCsv(line));
        return list;
    }

    public void saveBeverages(CustomLinkedList<Beverage> beverages) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id,name,price,stock,soldOutDate");
        for (Beverage b : beverages) lines.add(b.toCsv());
        Files.write(beverageFile, lines);
    }

    public List<SalesRecord> loadSales() throws IOException {
        List<SalesRecord> records = new ArrayList<>();
        for (String line : Files.readAllLines(salesFile)) if (!line.isBlank() && !line.startsWith("date,")) records.add(SalesRecord.fromCsv(line));
        return records;
    }

    public void appendSale(SalesRecord record) throws IOException {
        Files.writeString(salesFile, record.toCsv() + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public String loadPassword() throws IOException { return Files.readString(adminFile).trim(); }
    public void savePassword(String password) throws IOException { Files.writeString(adminFile, password + "\n"); }
    public void audit(String message) throws IOException { Files.writeString(auditFile, LocalDate.now() + " " + message + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND); }

    private CustomLinkedList<Beverage> defaultBeverages() {
        CustomLinkedList<Beverage> list = new CustomLinkedList<>();
        list.add(new Beverage(1, "믹스커피", 200, 10));
        list.add(new Beverage(2, "고급믹스커피", 300, 10));
        list.add(new Beverage(3, "물", 450, 10));
        list.add(new Beverage(4, "캔커피", 500, 10));
        list.add(new Beverage(5, "이온음료", 550, 10));
        list.add(new Beverage(6, "고급캔커피", 700, 10));
        list.add(new Beverage(7, "탄산음료", 750, 10));
        list.add(new Beverage(8, "특화음료", 800, 10));
        return list;
    }
}
