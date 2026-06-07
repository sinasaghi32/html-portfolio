package file;
import exception.FileDataException;
import model.SaleRecord;
import java.nio.file.Path;
import java.util.*;
public class SalesFileManager extends FileManager{
    private final Path daily=dataDir.resolve("sales_daily.txt"), monthly=dataDir.resolve("sales_monthly.txt");
    public void appendSale(SaleRecord r) throws FileDataException { append(daily,r.toCsv()); append(monthly,r.toCsv()); }
    public List<SaleRecord> loadAll() throws FileDataException { List<SaleRecord> out=new ArrayList<>(); for(String s:readAll(daily)) if(!s.isBlank()) out.add(SaleRecord.fromCsv(s)); return out; }
}
