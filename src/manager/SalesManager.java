package manager;
import datastructure.MyStack;
import exception.FileDataException;
import file.SalesFileManager;
import model.SaleRecord;
import java.time.LocalDate;
import java.util.*;
public class SalesManager{
    private final SalesFileManager file=new SalesFileManager(); private final MyStack<SaleRecord> recent=new MyStack<>();
    public SalesManager(){ try{ for(SaleRecord r:file.loadAll()) recent.push(r); }catch(Exception ignored){} }
    public void record(SaleRecord r) throws FileDataException{ recent.push(r); file.appendSale(r); }
    public List<SaleRecord> loadAll() throws FileDataException{ return file.loadAll(); }
    public List<SaleRecord> recent(int max){ return recent.recent(max); }
    public String dailySummary() throws FileDataException{ return summary(LocalDate.now().toString(), true); }
    public String monthlySummary() throws FileDataException{ return summary(LocalDate.now().toString().substring(0,7), false); }
    private String summary(String key, boolean daily) throws FileDataException{ int total=0; Map<String,Integer> byDrink=new HashMap<>(); for(SaleRecord r:loadAll()){ String k=daily?r.day():r.month(); if(k.equals(key)){ total+=r.getTotal(); byDrink.put(r.getDrinkName(),byDrink.getOrDefault(r.getDrinkName(),0)+r.getTotal()); }} StringBuilder sb=new StringBuilder((daily?"일일":"월간")+" 총 매출: "+total+"원\n"); for(String n:byDrink.keySet()) sb.append(n).append(": ").append(byDrink.get(n)).append("원\n"); return sb.toString(); }
}
